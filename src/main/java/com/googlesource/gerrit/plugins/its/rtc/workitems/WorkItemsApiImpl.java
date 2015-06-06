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
import java.lang.reflect.Type;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;

import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import com.googlesource.gerrit.plugins.its.rtc.api.ResourceInvalidException;
import com.googlesource.gerrit.plugins.its.rtc.api.RtcEntity;
import com.googlesource.gerrit.plugins.its.rtc.network.RTCClient;
import com.googlesource.gerrit.plugins.its.rtc.network.Transport;

public class WorkItemsApiImpl extends AbstractApiImpl implements WorkItemsApi {

  private Transport transport;
  public WorkItemsApiImpl(RTCClient rtcClient, Transport transport) {
    super(rtcClient);
    this.transport = transport;
  }

  @Override
  public synchronized RtcWorkItem getWorkItem(long id) throws IOException {
    loginIfNeeded();
    return transport.get("/oslc/workitems/" + id + ".json", RtcWorkItem.class);
  }

  @Override
  public synchronized RtcComment addComment(long id, String text)
      throws IOException {
    loginIfNeeded();
    return transport.post("/oslc/workitems/" + id + "/rtc_cm:comments",
        RtcComment.class, 
        Transport.APP_JSON,
        new BasicNameValuePair("dc:description", text));
  }

  @Override
  public synchronized RtcRelatedLink addRelated(long id, URL relatedUrl,
      String text) throws IOException {
    loginIfNeeded();
    return transport
        .post(
            "/oslc/workitems/"
                + id
                + "/rtc_cm:com.ibm.team.workitem.linktype.relatedartifact.relatedArtifact",
            RtcRelatedLink.class, 
            Transport.APP_JSON,
            new BasicNameValuePair("rdf:resource",
                relatedUrl.toExternalForm()), new BasicNameValuePair(
                "oslc_cm:label", text));
  }

  @Override
  public List<RtcEntity> getAvailableStatuses(RtcWorkItem wip)
      throws IOException {
    loginIfNeeded();
    final String rdf = wip.getStatus().getRdf();
    final String url = rdf.substring(0, rdf.lastIndexOf('/')) + ".json";
    final Type type = new TypeToken<Collection<RtcEntity>>() {}.getType();
    return transport.get(url, type);
  }

  @Override
  public List<RtcWorkflowAction> getAvailableActions(RtcWorkItem wip)
      throws IOException {
    loginIfNeeded();
    final String rdf = wip.getStatus().getRdf();
    final String url = rdf.substring(0, rdf.lastIndexOf('/')).replace("states", "actions")+ ".json";
    final Type type = new TypeToken<Collection<RtcWorkflowAction>>() {}.getType();
    return transport.get(url, type);
  }

  @Override
  public RtcWorkItem performAction(RtcWorkItem wip, String actionTitle)
      throws IOException {
    loginIfNeeded();
    RtcWorkflowAction action = null;
    List<RtcWorkflowAction> allActions = getAvailableActions(wip);
    for (RtcWorkflowAction anAction : allActions) {
      if (anAction.getTitle().equalsIgnoreCase(actionTitle)) {
        action = anAction;
        break;
      }
    }

    if (action == null) {
      throw new ResourceInvalidException(actionTitle);
    }
    
    final String url = "/oslc/workitems/" + wip.getId()+"?_action="+action.getId();

    JsonObject rdf = new JsonObject();
    rdf.addProperty("rdf:resource", action.getResultStateRdf());
    JsonObject data = new JsonObject();
    data.add("rtc_cm:state", rdf);

    return transport.patch(url, RtcWorkItem.class, data, wip.getEtag());
  }
  
/*
  Object foo()  
  {   
//    String s = wip.getStatus().getRdf();
//    JsonObject rdf = new JsonObject();
//    rdf.addProperty("rdf:resource", s);
//    JsonObject data = new JsonObject();
//    data.add("rtc_cm:state", rdf);

    JsonObject rdf = new JsonObject();
    final String url = newStatus.getRdf();
    rdf.addProperty("rdf:resource", url);
    JsonObject data = new JsonObject();
    data.add("rtc_cm:state", rdf);

//    JsonObject data = new JsonObject();
//    data.addProperty("dc:title", "Hey, wombats! Is this so called 'patch' really working???");

    System.err.println(data);
    return transport.patch("/oslc/workitems/" + wip.getId(), RtcWorkItem.class, data, wip.getEtag());
//    return transport.put("/oslc/workitems/" + wip.getId()+"?oslc_cm.properties=rtc_cm:state", RtcWorkItem.class, data, wip.getEtag());
  }
*/
}
