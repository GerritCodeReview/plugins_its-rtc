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

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.googlesource.gerrit.plugins.its.rtc.api.AbstractDeserializer;

public class RtcRelatedLinkDeserializer extends AbstractDeserializer
    implements JsonDeserializer<RtcRelatedLink> {

  @Override
  public RtcRelatedLink deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    
    JsonObject jsonObj = json.getAsJsonObject();
    String resourceUrlString = jsonObj.get("rdf:resource").getAsString();
    try {
      URL resourceUrl = new URL(resourceUrlString);
      String label = jsonObj.get("oslc_cm:label").getAsString();
      
      return new RtcRelatedLink(resourceUrl, label);
      
    } catch (MalformedURLException e) {
      throw new JsonParseException("Invalid rdf:resource URL '" + resourceUrlString + "'", e);
    }
  }

}
