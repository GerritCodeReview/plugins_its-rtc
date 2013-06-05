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

import java.io.IOException;
import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.googlesource.gerrit.plugins.hooks.rtc.api.AbstractDeserializer;
import com.googlesource.gerrit.plugins.hooks.rtc.api.RtcEntity;
import com.googlesource.gerrit.plugins.hooks.rtc.network.CachableResourcesFactory;

public class RtcWorkItemDeserializer extends AbstractDeserializer implements JsonDeserializer<RtcWorkItem> {

  private CachableResourcesFactory factory;

  public RtcWorkItemDeserializer(CachableResourcesFactory factory) {
    this.factory = factory;
  }

  @Override
  public RtcWorkItem deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    
    JsonObject root = json.getAsJsonObject();

    RtcWorkItem result = new RtcWorkItem(extractRdf(root));
    result.id = extractLong(root, "dc:identifier");
    result.title = extractString(root,"dc:title");
    result.subject = extractString(root,"dc:subject");
    result.creator = extractIdenFromRdfResource(root, "dc:creator");
    result.ownedby = extractIdenFromRdfResource(root, "rtc_cm:ownedBy");
    result.status = extractResource(root, "rtc_cm:state", RtcEntity.class);
    result.severity = extractResource(root, "oslc_cm:severity", RtcEntity.class);
    result.priority = extractResource(root, "oslc_cm:priority", RtcEntity.class);
    result.type = extractResource(root, "dc:type", RtcEntity.class);
    
    
    return result;
  }

  private RtcEntity extractResource(JsonObject root, final String memberName,
      final Class<RtcEntity> clazz) {
    String url = extractRdfResourceUrl(root, memberName);
    try {
      return factory.get(url, clazz);
    } catch (IOException e) {
      throw new JsonParseException("Unable to parse resource from url "+url+" using class "+clazz, e);
    }
  }

}
