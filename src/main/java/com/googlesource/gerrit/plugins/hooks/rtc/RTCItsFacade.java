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
package com.googlesource.gerrit.plugins.hooks.rtc;

import java.io.IOException;
import java.net.URL;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.googlesource.gerrit.plugins.hooks.its.ItsFacade;
import com.googlesource.gerrit.plugins.hooks.rtc.network.RTCClient;
import com.googlesource.gerrit.plugins.hooks.rtc.workitems.RtcComment;
import com.googlesource.gerrit.plugins.hooks.rtc.workitems.RtcRelatedLink;
import com.googlesource.gerrit.plugins.hooks.rtc.workitems.RtcWorkItem;

public class RTCItsFacade implements ItsFacade {

  public static final String ITS_NAME_RTC = "rtc";

  public static final String GERRIT_CONFIG_RTC_USERNAME = "username";
  public static final String GERRIT_CONFIG_RTC_PASSWORD = "password";
  public static final String GERRIT_CONFIG_CCM_URL = "url";
  public static final String GERRIT_CONFIG_SSL_VERIFY = "sslVerify";

  private Logger log = LoggerFactory.getLogger(RTCItsFacade.class);

  Config gerritConfig;

  private RTCClient client;

  private Injector injector;

  @Inject
  public RTCItsFacade(@GerritServerConfig Config gerritConfig, Injector injector) {
    try {
      this.injector = injector;
      this.gerritConfig = gerritConfig;
      client().ping();
      log.info("Connected to RTC at " + getRtcUrl() + " as admin user "
          + getRtcUser());
    } catch (Exception ex) {
      log.warn("RTC is currently not available", ex);
    }
  }

  @Override
  public String name() {
    return "RTC";
  }

  @Override
  public void addComment(String itemId, String comment) throws IOException {
    long workItem = Long.parseLong(itemId);
    log.debug("Adding comment " + comment + " to workItem " + workItem);
    RtcComment rtcComment =
        client().workItemsApi().addComment(workItem, comment);
    log.debug("Comment created: " + rtcComment);
  }

  @Override
  public void addRelatedLink(String itemId, URL relatedUrl, String description)
      throws IOException {
    long workItem = Long.parseLong(itemId);
    log.debug("Adding related link " + relatedUrl + " to workItem " + workItem
        + " with description " + description);
    RtcRelatedLink relatedLink =
        client().workItemsApi().addRelated(workItem, relatedUrl, description);
    log.debug("Related link " + relatedLink + " to workItem#" + workItem
        + " CREATED");
  }

  @Override
  public void performAction(String itemId, String actionName)
      throws IOException {
    long workItem = Long.parseLong(itemId);
    log.debug("Executing action " + actionName + " on workItem " + workItem);
    RtcWorkItem wip = client().workItemsApi().getWorkItem(workItem);
    log.debug(" - loaded workitem " + wip);
    wip = client().workItemsApi().performAction(wip, actionName);
    log.debug("New item state: : " + wip);
  }

  @Override
  public String healthCheck(Check check) throws IOException {
    client.ping();
    return "{\"status\"=\"ok\",\"system\"=\"RTC\",}";
  }

  private RTCClient client() throws IOException {

    if (client == null) {
      client = injector.getInstance(RTCClient.class);
      client.setLoginCredentials(getRtcUser(), getRtcPassword());

      log.debug("RTC Client pointing to " + getRtcUrl() + " as " + getRtcUser());
    }

    return client;
  }

  private String getRtcPassword() {
    return gerritConfig.getString(ITS_NAME_RTC, null,
        GERRIT_CONFIG_RTC_PASSWORD);
  }

  private String getRtcUser() {
    return gerritConfig.getString(ITS_NAME_RTC, null,
        GERRIT_CONFIG_RTC_USERNAME);
  }

  private String getRtcUrl() {
    return gerritConfig.getString(ITS_NAME_RTC, null, GERRIT_CONFIG_CCM_URL);
  }

  private boolean getSslVerify() {
    return gerritConfig.getBoolean(ITS_NAME_RTC, null,
        GERRIT_CONFIG_SSL_VERIFY, true);
  }

  @Override
  public String createLinkForWebui(String url, String text) {
    return "<a href=\"" + url + "\">" + text + "</a>";
  }

  @Override
  public boolean exists(String itemId) throws IOException {
    long workItem = Long.parseLong(itemId);
    RtcWorkItem item = client().workItemsApi().getWorkItem(workItem);
    return item != null;
  }

}
