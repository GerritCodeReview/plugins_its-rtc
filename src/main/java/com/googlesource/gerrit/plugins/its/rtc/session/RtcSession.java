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

import com.googlesource.gerrit.plugins.its.rtc.api.RtcObject;
import java.util.Arrays;
import java.util.List;

public class RtcSession extends RtcObject {

  private String userId;
  private String[] roles;

  private RtcSession() {
    super();
  }

  public RtcSession(String userId, String[] roles) {
    this();
    this.userId = userId;
    this.roles = roles;
  }

  public String getUserId() {
    return userId;
  }

  public List<String> getRoles() {
    return Arrays.asList(roles);
  }
}
