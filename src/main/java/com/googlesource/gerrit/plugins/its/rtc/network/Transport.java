// Copyright (C) 2013 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.googlesource.gerrit.plugins.its.rtc.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpParams;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import com.googlesource.gerrit.plugins.its.rtc.api.ResourceNotFoundException;

public class Transport {

  public static final String APP_JSON = "application/json";
  public static final String ANY = "*/*";
  public static final String APP_OSLC =
      "application/x-oslc-cm-change-request+json";

  private static final Log log = LogFactory.getLog(Transport.class);

  public static final ThreadLocal<String> etag = new ThreadLocal<>();

  protected HttpClient httpclient;
  protected Gson gson;
  protected String baseUrl;
  private HttpParams httpParams;
  private RTCClient rtcClient;

  public Transport(RTCClient rtcClient, String baseUrl, DefaultHttpClient httpclient, HttpParams httpParams) {
    this.rtcClient = rtcClient;
    this.baseUrl = baseUrl;
    this.httpclient = httpclient;
    this.httpParams = httpParams;
  }

  public void setGson(Gson gson) {
    this.gson = gson;
  }

  public <T> T put(final String path, final Type typeOrClass, JsonObject data,
      String etag) throws IOException {
    HttpPut request = new HttpPut(toUri(path));
    if (log.isDebugEnabled())
      log.debug("Preparing PUT against " + request.getURI() + " using etag "
          + etag + " and data " + data);
    request.setEntity(new StringEntity(data.toString(), StandardCharsets.UTF_8));
    if (etag != null) request.addHeader("If-Match", etag);
    return invoke(request, typeOrClass, APP_OSLC, APP_OSLC);
  }

  public <T> T patch(final String path, final Type typeOrClass,
      JsonObject data, String etag) throws IOException {
    HttpPatch request = newHttpPatch(path);
    if (log.isDebugEnabled())
      log.debug("Preparing PATCH against " + request.getURI() + " using etag "
          + etag + " and data " + data);
    request.setEntity(new StringEntity(data.toString(), StandardCharsets.UTF_8));
    if (etag != null) request.addHeader("If-Match", etag);
    return invoke(request, typeOrClass, APP_OSLC, APP_OSLC);
  }

  public <T> T post(final String path, final Type typeOrClass,
      String contentType,
      final NameValuePair... params) throws IOException {
    HttpPost request = newHttpPost(path);
    if (log.isDebugEnabled())
      log.debug("Preparing POST against " + request.getURI() + " using params "
          + Arrays.asList(params));
    List<NameValuePair> nameValuePairs = Arrays.asList(params);
    request.setEntity(new UrlEncodedFormEntity(nameValuePairs, StandardCharsets.UTF_8));
    return invoke(request, typeOrClass, contentType, null);
  }

  public <T> T get(final String path, final Type typeOrClass)
      throws IOException, MalformedURLException {
    final HttpGet request = newHttpGet(path);
    if (log.isDebugEnabled())
      log.debug("Preparing GET against " + request.getURI());
    return invoke(request, typeOrClass, APP_JSON, null);
  }

  public String get(final String path) throws IOException,
      MalformedURLException {
    final HttpGet request = newHttpGet(path);
    if (log.isDebugEnabled())
      log.debug("Preparing GET against " + request.getURI());
    return invoke(request, null, ANY, null);
  }

  @SuppressWarnings("unchecked")
  private synchronized <T> T invoke(HttpRequestBase request,
      Object typeOrClass, String acceptType, String contentType)
      throws IOException, ClientProtocolException, ResourceNotFoundException {

    if (contentType != null) {
      request.addHeader("Content-Type", contentType);
    }

    if (acceptType != null) {
      request.addHeader("Accept", acceptType);
    }

    HttpResponse response = httpclient.execute(request);
    try {
      final int code = response.getStatusLine().getStatusCode();
      if (code / 100 != 2) {
        if (code == 404) {
          log.debug("API call failed: " + response.getStatusLine());
          throw new ResourceNotFoundException(request.getURI());
        } else {
          log.debug("API call failed: " + response.getStatusLine());
          throw new IOException("API call failed! " + response.getStatusLine());
        }
      }

      String responseContentTypeString = getResponseContentType(response);
      String entityString = readEntityAsString(response);

      if (!assertValidContentType(acceptType, responseContentTypeString)) {
        log.error("Request to " + request.getURI()
            + " failed because of an invalid content returned:\n"
            + entityString);
        rtcClient.setLoggedIn(false);
        throw new InvalidContentTypeException("Wrong content type '"
            + responseContentTypeString + "' in HTTP response (Expected: "
            + acceptType + ")");
      }

      if (typeOrClass != null && acceptType.endsWith("json")
          && responseContentTypeString.endsWith("json")) {
        Transport.etag.set(extractEtag(response));
        if (typeOrClass instanceof ParameterizedType) {
          return gson.fromJson(entityString, (Type) typeOrClass);
        } else {
          return gson.fromJson(entityString, (Class<T>) typeOrClass);
        }
      } else if (typeOrClass != null && typeOrClass.equals(String.class)) {
        return (T) entityString;
      } else {
        if (log.isDebugEnabled()) log.debug(entityString);
        return null;
      }
    } finally {
      consumeHttpEntity(response.getEntity());
      Transport.etag.set(null);
    }
  }

  private boolean assertValidContentType(String acceptType,
      String responseContentTypeString) {
    if (acceptType == null) {
      return true;
    }
    if (acceptType.endsWith("/*")) {
      return true;
    }
    if (acceptType.split("/")[1].equalsIgnoreCase(responseContentTypeString
        .split("/")[1])) {
      return true;
    }
    return false;
  }

  public String getResponseContentType(HttpResponse response) {
    Header contentType = response.getEntity().getContentType();
    if (contentType == null) {
      return null;
    }

    String contentTypeValue = contentType.getValue();
    if (contentTypeValue == null) {
      return null;
    }

    for (String contentTypeItem : contentTypeValue.split(";")) {
      if (contentTypeItem.indexOf('/') >= 0) {
        return contentTypeItem;
      }
    }
    return null;
  }

  private String readEntityAsString(HttpResponse response)
      throws IllegalStateException, IOException {
    String charset = "utf-8";
    Header[] contentTypes = response.getHeaders("Content-Type");
    for (Header header : contentTypes) {
      if (header.getName().equalsIgnoreCase("charset")) {
        charset = header.getValue();
      }
    }

    ByteArrayOutputStream responseOut = new ByteArrayOutputStream();
    try {
      IOUtils.copy(response.getEntity().getContent(), responseOut);
    } finally {
      responseOut.close();
    }

    return new String(responseOut.toByteArray(), Charset.forName(charset));
  }

  // We can't use the HTTP Client 4.2 EntityUtils.consume()
  // because of compatibility issues with gerrit-pgm 2.5
  // that includes httpclient 4.0
  private void consumeHttpEntity(final HttpEntity entity) throws IOException {
    if (entity == null) {
      return;
    }
    if (entity.isStreaming()) {
      InputStream instream = entity.getContent();
      if (instream != null) {
        instream.close();
      }
    }
  }

  private String extractEtag(HttpResponse response) {
    final Header etagHeader = response.getFirstHeader("ETag");
    return etagHeader == null ? null : etagHeader.getValue().substring(1,
        etagHeader.getValue().length() - 1);
  }

  private HttpGet newHttpGet(final String path) {
    HttpGet get = new HttpGet(toUri(path));
    get.setParams(httpParams);
    return get;
  }

  private HttpPost newHttpPost(final String path) {
    return new HttpPost(toUri(path));
  }

  private HttpPatch newHttpPatch(final String path) {
    return new HttpPatch(toUri(path));
  }

  private String toUri(final String path) {
    if (path.startsWith(baseUrl))
      return path;
    else
      return baseUrl + path;
  }
}
