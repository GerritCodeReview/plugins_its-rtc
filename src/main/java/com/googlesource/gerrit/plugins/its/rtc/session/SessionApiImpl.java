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
package com.googlesource.gerrit.plugins.its.rtc.session;

import java.io.IOException;

import org.apache.http.message.BasicNameValuePair;

import com.googlesource.gerrit.plugins.its.rtc.network.AuthenticationException;
import com.googlesource.gerrit.plugins.its.rtc.network.RTCClient;
import com.googlesource.gerrit.plugins.its.rtc.network.Transport;
import com.googlesource.gerrit.plugins.its.rtc.workitems.AbstractApiImpl;

public class SessionApiImpl extends AbstractApiImpl implements SessionApi {

  private final Transport transport;

  public SessionApiImpl(RTCClient rtcClient, Transport transport) {
    super(rtcClient);
    this.transport=transport;
  }

  @Override
  public synchronized void login(String username, String password)
      throws IOException {
    String result = transport.post("/authenticated/j_security_check", String.class,
        Transport.ANY,
        new BasicNameValuePair("j_username", username), 
        new BasicNameValuePair("j_password", password));
    
    if(result.indexOf("net.jazz.web.app.authfailed") > 0) {
      throw new AuthenticationException("User authentication failed");
    }
  }

  @Override
  public void ping() throws IOException {
    loginIfNeeded();
    transport.get("/authenticated/identity");
  }
}
