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
package com.googlesource.gerrit.plugins.its.rtc;

import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.pgm.init.api.AllProjectsConfig;
import com.google.gerrit.pgm.init.api.AllProjectsNameOnInitProvider;
import com.google.gerrit.pgm.init.api.InitFlags;
import com.google.gerrit.pgm.init.api.Section;
import com.google.gerrit.pgm.init.api.ConsoleUI;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import com.googlesource.gerrit.plugins.its.base.its.InitIts;
import com.googlesource.gerrit.plugins.its.base.validation.ItsAssociationPolicy;
import com.googlesource.gerrit.plugins.its.rtc.network.RTCClient;

import org.eclipse.jgit.errors.ConfigInvalidException;

import java.io.IOException;
import java.util.Arrays;

/** Initialize the GitRepositoryManager configuration section. */
@Singleton
class InitRTC extends InitIts {
  private static final String COMMENT_LINK_SECTION = "commentLink";
  private final String pluginName;
  private final InitFlags flags;
  private Section rtc;
  private Section rtcComment;
  private Section.Factory sections;
  private String rtcUrl;
  private String rtcUsername;
  private String rtcPassword;

  @Inject
  InitRTC(@PluginName String pluginName, ConsoleUI ui,
      Section.Factory sections, AllProjectsConfig allProjectsConfig,
      AllProjectsNameOnInitProvider allProjects, InitFlags flags) {
    super(pluginName, "IBM Rational Team Concert", ui,
        allProjectsConfig, allProjects);
    this.pluginName = pluginName;
    this.sections = sections;
    this.flags = flags;
  }

  @Override
  public void run() throws IOException, ConfigInvalidException {
    super.run();

    ui.message("\n");
    ui.header("IBM Rational Team Concert connectivity");

    if (!pluginName.equalsIgnoreCase("rtc")
        && !flags.cfg.getSections().contains(pluginName)
        && flags.cfg.getSections().contains("rtc")) {
      ui.message("A RTC configuration for the 'hooks-rtc' plugin was found.\n");
      if (ui.yesno(true, "Copy it for the '%s' plugin?", pluginName)) {
        for (String n : flags.cfg.getNames("rtc")) {
          flags.cfg.setStringList(pluginName, null, n,
              Arrays.asList(flags.cfg.getStringList("rtc", null, n)));
        }
        for (String n : flags.cfg.getNames(COMMENT_LINK_SECTION, "rtc")) {
          flags.cfg.setStringList(COMMENT_LINK_SECTION, pluginName, n,
              Arrays.asList(flags.cfg.getStringList(COMMENT_LINK_SECTION, "rtc", n)));
        }

        if (ui.yesno(false, "Remove configuration for 'hooks-rtc' plugin?")) {
          flags.cfg.unsetSection("rtc", null);
          flags.cfg.unsetSection(COMMENT_LINK_SECTION, "rtc");
        }
      } else {
        init();
      }
    } else {
      init();
    }
  }

  private void init() {
    this.rtc = sections.get(pluginName, null);
    this.rtcComment =
        sections.get(COMMENT_LINK_SECTION, pluginName);

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
