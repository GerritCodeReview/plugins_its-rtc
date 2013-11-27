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

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.pgm.init.AllProjectsConfig;
import com.google.gerrit.pgm.init.Section;
import com.google.gerrit.pgm.util.ConsoleUI;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.hooks.its.InitIts;
import com.googlesource.gerrit.plugins.hooks.rtc.network.RTCClient;
import com.googlesource.gerrit.plugins.hooks.validation.ItsAssociationPolicy;

import org.eclipse.jgit.errors.ConfigInvalidException;

import java.io.IOException;

/** Initialize the GitRepositoryManager configuration section. */
@Singleton
class InitRTC extends InitIts {
  private static final String COMMENT_LINK_SECTION = "commentLink";
  private final String pluginName;
  private Section rtc;
  private Section rtcComment;
  private Section.Factory sections;
  private String rtcUrl;
  private String rtcUsername;
  private String rtcPassword;

  @Inject
  InitRTC(@PluginName String pluginName, ConsoleUI ui, Section.Factory sections,
      AllProjectsConfig allProjectsConfig) {
    super(pluginName, "IBM Rational Team Concert", ui,
        allProjectsConfig);
    this.pluginName = pluginName;
    this.sections = sections;
  }

  public void postRun() throws IOException, ConfigInvalidException {
    super.postRun();

    this.rtc = sections.get(pluginName, null);
    this.rtcComment =
        sections.get(COMMENT_LINK_SECTION, pluginName);

    ui.message("\n");
    ui.header("IBM Rational Team Concert connectivity");

    boolean sslVerify = true;
    do {
      rtcUrl = enterRTCConnectivity();
      if(rtcUrl != null) {
        sslVerify = enterSSLVerify(rtc);
      }
    } while (rtcUrl != null
        && (isConnectivityRequested(rtcUrl) && !isRTCConnectSuccessful(rtcUrl, sslVerify)));

    if (rtcUrl == null) {
      return;
    }

    ui.header("Rational Team Concert issue-tracking association");
    rtcComment.string("RTC Issue-Id regex", "match", "RTC#([0-9]+)");
    rtcComment.set("html",
        String.format("<a href=\"%s/browse/$1\">$1</a>", rtcUrl));

    rtcComment.select("RTC Issue-Id enforced in commit message", "association",
        ItsAssociationPolicy.OPTIONAL);
  }

  public String enterRTCConnectivity() {
    String url = rtc.string("RTC CCM URL (empty to skip)", "url", null);
    if (url != null) {
      rtcUsername = rtc.string("RTC username", "username", "");
      rtcPassword = rtc.password("username", "password");
    }
    return url;
  }

  private boolean isRTCConnectSuccessful(String rtcUrl, boolean sslVerify) {
    ui.message("Checking IBM Rational Team Concert connectivity ... ");
    try {
      RTCClient rtcClient = new RTCClient(rtcUrl, sslVerify, null);
      rtcClient.sessionApi().login(rtcUsername, rtcPassword);
      ui.message("[OK]\n");
      return true;
    } catch (IOException e) {
      ui.message("*FAILED* (%s)\n", e.getLocalizedMessage());
      return false;
    }
  }
}
