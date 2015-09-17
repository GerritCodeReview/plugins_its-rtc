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

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.eclipse.jgit.lib.Config;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.its.rtc.api.RtcEntity;
import com.googlesource.gerrit.plugins.its.rtc.api.RtcEntityDeserializer;
import com.googlesource.gerrit.plugins.its.rtc.session.SessionApi;
import com.googlesource.gerrit.plugins.its.rtc.session.SessionApiImpl;
import com.googlesource.gerrit.plugins.its.rtc.workitems.RtcComment;
import com.googlesource.gerrit.plugins.its.rtc.workitems.RtcCommentDeserializer;
import com.googlesource.gerrit.plugins.its.rtc.workitems.RtcRelatedLink;
import com.googlesource.gerrit.plugins.its.rtc.workitems.RtcRelatedLinkDeserializer;
import com.googlesource.gerrit.plugins.its.rtc.workitems.RtcWorkItem;
import com.googlesource.gerrit.plugins.its.rtc.workitems.RtcWorkItemDeserializer;
import com.googlesource.gerrit.plugins.its.rtc.workitems.RtcWorkflowAction;
import com.googlesource.gerrit.plugins.its.rtc.workitems.RtcWorkflowActionDeserializer;
import com.googlesource.gerrit.plugins.its.rtc.workitems.WorkItemsApi;
import com.googlesource.gerrit.plugins.its.rtc.workitems.WorkItemsApiImpl;

public class RTCClient {

  private static Log LOG = LogFactory.getLog(RTCClient.class);

  private String baseUrl;
  private DefaultHttpClient httpclient;
  private BasicCookieStore cookieStore;
  private Gson gson;

  private Transport transport;
  private CachableResourcesFactory factory;

  private SessionApi sessionApi;
  private WorkItemsApi workItemsApi;

  private boolean loggedIn;

  private String rtcUser;

  private String rtcPassword;

  @Inject
  public RTCClient(@PluginName String pluginName,
      @GerritServerConfig Config config, RTCHttpParams httpParams)
      throws IOException {
    this(config.getString(pluginName, null, "url"), config
        .getBoolean(pluginName, null, "sslVerify", true),
        httpParams);
  }

  public RTCClient(String url, boolean sslVerify, HttpParams httpParams)
      throws IOException {
    super();

    this.baseUrl = (url.endsWith("/") ? url.substring(url.length() - 1) : url);
    if (httpParams == null) {
      httpParams = new BasicHttpParams();
    }
    SchemeRegistry schemeRegistry = new SchemeRegistry();
    schemeRegistry.register(new Scheme("http", PlainSocketFactory
        .getSocketFactory(), 80));
    this.httpclient =
        new DefaultHttpClient(new ThreadSafeClientConnManager(httpParams,
            schemeRegistry), httpParams);

    this.transport = new Transport(this, baseUrl, httpclient, httpParams);
    this.factory = new CachableResourcesFactory(transport);

    GsonBuilder builder = new GsonBuilder();
    builder.registerTypeAdapter(RtcWorkItem.class, new RtcWorkItemDeserializer(
        factory));
    builder.registerTypeAdapter(RtcComment.class, new RtcCommentDeserializer());
    builder.registerTypeAdapter(RtcEntity.class, new RtcEntityDeserializer());
    builder.registerTypeAdapter(RtcRelatedLink.class,
        new RtcRelatedLinkDeserializer());
    builder.registerTypeAdapter(RtcWorkflowAction.class,
        new RtcWorkflowActionDeserializer());
    gson = builder.create();
    transport.setGson(gson);

    setCookieStore();
    setRedirectStategy();
    setSSLTrustStrategy(sslVerify);

    sessionApi = new SessionApiImpl(this, transport);
    workItemsApi = new WorkItemsApiImpl(this, transport);
  }

  public boolean isLoggedIn() {
    return loggedIn;
  }

  public SessionApi sessionApi() throws IOException {
    collectSessionCookie();
    return sessionApi;
  }

  public WorkItemsApi workItemsApi() throws IOException {
    collectSessionCookie();
    return workItemsApi;
  }

  private void collectSessionCookie() throws IOException {
    if (cookieStore.getCookies().size() <= 0) {
      LOG.debug("Initial collecting of session cookie...");
      transport.get(baseUrl);
      LOG.debug("Succesfully collected cookies: " + cookieStore.getCookies());
    }
  }

  private void setCookieStore() {
    cookieStore = new BasicCookieStore();
    httpclient.setCookieStore(cookieStore);
  }

  private void setSSLTrustStrategy(boolean sslVerify) throws IOException {
    try {
      TrustManager[] trustAllCerts =
          new TrustManager[] {new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs,
                String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs,
                String authType) {
            }
          }};
      SSLContext sc;

      if (sslVerify) {
        sc = SSLContext.getDefault();
      } else {
        sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new SecureRandom());
      }

      SSLSocketFactory sf = new SSLSocketFactory(sc);
      sf.setHostnameVerifier(new AllowAllHostnameVerifier());
      SchemeRegistry schemeRegistry =
          httpclient.getConnectionManager().getSchemeRegistry();
      schemeRegistry.register(new Scheme("https", sf, 443));
    } catch (Exception any) {
      throw new IOException(any);
    }
  }

  private void setRedirectStategy() {
    httpclient.setRedirectHandler(new DefaultRedirectHandler() {
      @Override
      public boolean isRedirectRequested(HttpResponse response,
          HttpContext context) {
        boolean isRedirect = super.isRedirectRequested(response, context);
        if (!isRedirect) {
          int responseCode = response.getStatusLine().getStatusCode();
          if (responseCode == 301 || responseCode == 302) {
            return true;
          }
        }
        return isRedirect;
      }
    });
  }

  public Transport getTransportForTest() {
    return transport;
  }

  public void setLoginCredentials(String rtcUser, String rtcPassword) {
    this.rtcUser = rtcUser;
    this.rtcPassword = rtcPassword;
  }

  public void login() throws IOException {
    sessionApi().login(rtcUser, rtcPassword);
    loggedIn = true;
  }

  public void ping() throws IOException {
    if (loggedIn) {
      sessionApi().ping();
    }
  }

  public void setLoggedIn(boolean b) {
    loggedIn = b;
  }
}
