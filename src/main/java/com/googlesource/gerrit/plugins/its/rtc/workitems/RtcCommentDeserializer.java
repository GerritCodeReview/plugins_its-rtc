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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import com.googlesource.gerrit.plugins.its.rtc.api.AbstractDeserializer;

public class RtcCommentDeserializer extends AbstractDeserializer implements JsonDeserializer<RtcComment> {

  @Override
  public RtcComment deserialize(JsonElement json, Type typeOfT,
      JsonDeserializationContext context) throws JsonParseException {
    
    JsonObject root = json.getAsJsonObject();

    RtcComment result = new RtcComment(extractRdf(root));
    result.contents = extractString(root, "dc:description");
    result.creator = extractIdenFromRdfResource(root, "dc:creator");
    
    return result;
  }
}
