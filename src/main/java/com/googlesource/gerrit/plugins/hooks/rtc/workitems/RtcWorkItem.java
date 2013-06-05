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

import com.googlesource.gerrit.plugins.hooks.rtc.api.RtcEntity;
import com.googlesource.gerrit.plugins.hooks.rtc.api.RtcObject;


public class RtcWorkItem extends RtcObject {

  Long id;
  String title;
  String subject;
  String creator;
  String ownedby;
  String description;
  Calendar created;
  RtcEntity status;
  RtcEntity severity;
  RtcEntity priority;
  RtcEntity type;
//  private List<RtcComment> comments;    //
  
  RtcWorkItem(String rdf) {
    super(rdf);
  }

  public Long getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getSubject() {
    return subject;
  }

  public String getCreator() {
    return creator;
  }

  public String getOwnedby() {
    return ownedby;
  }

  public String getDescription() {
    return description;
  }

  public Calendar getCreated() {
    return created;
  }

  public RtcEntity getStatus() {
    return status;
  }

  public RtcEntity getSeverity() {
    return severity;
  }

  public RtcEntity getPriority() {
    return priority;
  }

  public RtcEntity getType() {
    return type;
  }
}
