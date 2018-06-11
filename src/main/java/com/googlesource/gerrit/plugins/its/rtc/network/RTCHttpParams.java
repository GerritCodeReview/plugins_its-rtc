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

import com.google.common.base.MoreObjects;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;
import java.util.HashMap;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.eclipse.jgit.lib.Config;

@SuppressWarnings("deprecation")
public class RTCHttpParams implements HttpParams {
  private static interface ParameterParser {
    public Object parse(String value);
  }

  private static final ParameterParser TYPE_STRING =
      new ParameterParser() {
        @Override
        public Object parse(String value) {
          return value;
        }
      };

  private static final ParameterParser TYPE_LONG =
      new ParameterParser() {
        @Override
        public Object parse(String value) {
          if (value == null) {
            return null;
          } else {
            return Long.parseLong(value);
          }
        }
      };

  private static final ParameterParser TYPE_INT =
      new ParameterParser() {
        @Override
        public Object parse(String value) {
          if (value == null) {
            return null;
          } else {
            return Integer.parseInt(value);
          }
        }
      };

  private static final ParameterParser TYPE_BOOL =
      new ParameterParser() {
        @Override
        public Object parse(String value) {
          if (value == null) {
            return null;
          } else {
            return Boolean.parseBoolean(value);
          }
        }
      };

  private static final HashMap<String, ParameterParser> TYPES = new HashMap<>();

  static {
    TYPES.put(CoreConnectionPNames.SO_TIMEOUT, TYPE_INT);
    TYPES.put(CoreConnectionPNames.TCP_NODELAY, TYPE_BOOL);
    TYPES.put(CoreConnectionPNames.SOCKET_BUFFER_SIZE, TYPE_INT);
    TYPES.put(CoreConnectionPNames.SO_LINGER, TYPE_INT);
    TYPES.put(CoreConnectionPNames.SO_REUSEADDR, TYPE_BOOL);
    TYPES.put(CoreConnectionPNames.CONNECTION_TIMEOUT, TYPE_INT);
    TYPES.put(CoreConnectionPNames.STALE_CONNECTION_CHECK, TYPE_BOOL);
    TYPES.put(CoreConnectionPNames.MAX_LINE_LENGTH, TYPE_INT);
    TYPES.put(CoreConnectionPNames.MAX_HEADER_COUNT, TYPE_INT);
    TYPES.put(CoreConnectionPNames.MIN_CHUNK_LIMIT, TYPE_INT);
    TYPES.put(CoreConnectionPNames.SO_KEEPALIVE, TYPE_BOOL);

    TYPES.put(ClientPNames.HANDLE_REDIRECTS, TYPE_BOOL);
    TYPES.put(ClientPNames.REJECT_RELATIVE_REDIRECT, TYPE_BOOL);
    TYPES.put(ClientPNames.MAX_REDIRECTS, TYPE_INT);
    TYPES.put(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, TYPE_BOOL);
    TYPES.put(ClientPNames.HANDLE_AUTHENTICATION, TYPE_BOOL);
    TYPES.put(ClientPNames.CONN_MANAGER_TIMEOUT, TYPE_LONG);

    TYPES.put(CoreProtocolPNames.STRICT_TRANSFER_ENCODING, TYPE_BOOL);
    TYPES.put(CoreProtocolPNames.USE_EXPECT_CONTINUE, TYPE_BOOL);
    TYPES.put(CoreProtocolPNames.WAIT_FOR_CONTINUE, TYPE_INT);

    TYPES.put(ConnManagerPNames.TIMEOUT, TYPE_LONG);
    TYPES.put(ConnManagerPNames.MAX_TOTAL_CONNECTIONS, TYPE_INT);
  }

  private final String pluginName;
  private Config config;

  @Inject
  public RTCHttpParams(@PluginName String pluginName, @GerritServerConfig Config config) {
    this.pluginName = pluginName;
    this.config = config;
  }

  private String getParamName(String name) {
    StringBuilder camelisedName = new StringBuilder();
    for (String namePart : name.split("[\\.-]")) {
      if (camelisedName.length() > 0) {
        camelisedName.append(Character.toUpperCase(namePart.charAt(0)));
        camelisedName.append(namePart.substring(1, namePart.length()));
      } else {
        camelisedName.append(namePart);
      }
    }
    return camelisedName.toString();
  }

  @Override
  public Object getParameter(String name) {
    String value = config.getString(pluginName, null, getParamName(name));
    return getParameterParser(name).parse(value);
  }

  private ParameterParser getParameterParser(String name) {
    return MoreObjects.firstNonNull(TYPES.get(name), TYPE_STRING);
  }

  @Override
  public HttpParams setParameter(String name, Object value) {
    return throwsNotSupported(HttpParams.class);
  }

  private <T> T throwsNotSupported(Class<T> clazz) {
    throw new IllegalArgumentException("Method not supported");
  }

  @Override
  @Deprecated
  public HttpParams copy() {
    return throwsNotSupported(HttpParams.class);
  }

  @Override
  public boolean removeParameter(String name) {
    return throwsNotSupported(Boolean.class);
  }

  @Override
  public long getLongParameter(String name, long defaultValue) {
    return config.getLong(pluginName, null, getParamName(name), defaultValue);
  }

  @Override
  public HttpParams setLongParameter(String name, long value) {
    return throwsNotSupported(HttpParams.class);
  }

  @Override
  public int getIntParameter(String name, int defaultValue) {
    return config.getInt(pluginName, null, getParamName(name), defaultValue);
  }

  @Override
  public HttpParams setIntParameter(String name, int value) {
    return throwsNotSupported(HttpParams.class);
  }

  @Override
  public double getDoubleParameter(String name, double defaultValue) {
    return throwsNotSupported(Double.class);
  }

  @Override
  public HttpParams setDoubleParameter(String name, double value) {
    return throwsNotSupported(HttpParams.class);
  }

  @Override
  public boolean getBooleanParameter(String name, boolean defaultValue) {
    return config.getBoolean(pluginName, null, getParamName(name), defaultValue);
  }

  @Override
  public HttpParams setBooleanParameter(String name, boolean value) {
    return throwsNotSupported(HttpParams.class);
  }

  @Override
  public boolean isParameterTrue(String name) {
    return getBooleanParameter(name, false);
  }

  @Override
  public boolean isParameterFalse(String name) {
    return !isParameterTrue(name);
  }
}
