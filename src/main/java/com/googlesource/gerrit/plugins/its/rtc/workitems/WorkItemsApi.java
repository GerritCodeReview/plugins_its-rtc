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
package com.googlesource.gerrit.plugins.its.rtc.workitems;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import com.googlesource.gerrit.plugins.its.rtc.api.RtcEntity;

public interface WorkItemsApi {

  public RtcWorkItem getWorkItem(long id) throws IOException;

  public RtcComment addComment(long id, String text) throws IOException;

  public RtcRelatedLink addRelated(long id, URL relatedUrl, String text)
      throws IOException;

  public List<RtcEntity> getAvailableStatuses(RtcWorkItem wip) throws IOException;

  public List<RtcWorkflowAction> getAvailableActions(RtcWorkItem wip) throws IOException;

  public RtcWorkItem performAction(RtcWorkItem wip, String newStatusTitle) throws IOException;

}
