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
package com.googlesource.gerrit.plugins.hooks.rtc.workitems;

import java.util.Calendar;

import com.googlesource.gerrit.plugins.hooks.rtc.api.RtcObject;

public class RtcComment extends RtcObject {

  String creator;
  String contents;
  Calendar created;
  
  RtcComment(String rdf) {
    super(rdf);
  }

  public RtcComment(String creator, String contents) {
    super();
    this.creator = creator;
    this.contents = contents;
  }

  public String getCreator() {
    return creator;
  }

  public String getContents() {
    return contents;
  }

  public Calendar getCreated() {
    return created;
  }
}
