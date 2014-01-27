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

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.common.ChangeListener;
import com.google.gerrit.extensions.annotations.Exports;
import com.google.gerrit.extensions.annotations.PluginName;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.PluginConfigFactory;
import com.google.gerrit.server.config.ProjectConfigEntry;
import com.google.gerrit.server.git.validators.CommitValidationListener;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import com.googlesource.gerrit.plugins.hooks.its.ItsFacade;
import com.googlesource.gerrit.plugins.hooks.its.ItsHookEnabledConfigEntry;
import com.googlesource.gerrit.plugins.hooks.rtc.filters.RTCAddComment;
import com.googlesource.gerrit.plugins.hooks.rtc.filters.RTCAddRelatedLinkToChangeId;
import com.googlesource.gerrit.plugins.hooks.rtc.filters.RTCAddRelatedLinkToGitWeb;
import com.googlesource.gerrit.plugins.hooks.rtc.filters.RTCChangeState;
import com.googlesource.gerrit.plugins.hooks.validation.ItsValidateComment;

public class RTCModule extends AbstractModule {

  private static final Logger LOG = LoggerFactory.getLogger(RTCModule.class);
  private static final int THREAD_POOL_EXECUTORS = 10;

  private final String pluginName;
  private final Config gerritConfig;
  private final PluginConfigFactory pluginCfgFactory;

  @Inject
  public RTCModule(@PluginName String pluginName,
      @GerritServerConfig Config config, PluginConfigFactory pluginCfgFactory) {
    this.pluginName = pluginName;
    this.gerritConfig = config;
    this.pluginCfgFactory = pluginCfgFactory;
  }

  @Override
  protected void configure() {
    if (isConfigPresent(pluginName)) {
      LOG.info("RTC is configured as ITS");
      bind(ItsFacade.class).to(RTCItsFacade.class);

      bind(ProjectConfigEntry.class)
          .annotatedWith(Exports.named("enabled"))
          .toInstance(new ItsHookEnabledConfigEntry(
              pluginName, pluginCfgFactory));

      DynamicSet.bind(binder(), CommitValidationListener.class).to(
          ItsValidateComment.class);

      bind(ExecutorService.class).toInstance(
          new ScheduledThreadPoolExecutor(THREAD_POOL_EXECUTORS));

      DynamicSet.bind(binder(), ChangeListener.class).to(
          RTCAddRelatedLinkToChangeId.class);
      DynamicSet.bind(binder(), ChangeListener.class).to(RTCAddComment.class);
      DynamicSet.bind(binder(), ChangeListener.class).to(RTCChangeState.class);
      DynamicSet.bind(binder(), ChangeListener.class).to(
          RTCAddRelatedLinkToGitWeb.class);
    }
  }

  private boolean isConfigPresent(String sectionName) {
    Set<String> names = gerritConfig.getSections();
    for (String name : names) {
      if (name.equals(sectionName)) {
        return true;
      }
    }
    return false;
  }
}
