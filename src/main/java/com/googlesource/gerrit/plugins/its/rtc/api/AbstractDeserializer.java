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
package com.googlesource.gerrit.plugins.its.rtc.api;

import com.google.gson.JsonObject;
import java.util.Calendar;
import javax.xml.bind.DatatypeConverter;

public class AbstractDeserializer {

  protected final String extractRdfResourceUrl(JsonObject json, String memberName) {
    JsonObject rdf = json.getAsJsonObject(memberName);
    return rdf.get("rdf:resource").getAsString();
  }

  protected final Calendar extractDateTime(JsonObject root, final String memberName) {
    String txt = extractString(root, memberName);
    return DatatypeConverter.parseDateTime(txt);
  }

  protected final String extractString(JsonObject root, final String memberName) {
    return root.get(memberName).getAsString();
  }

  protected final Long extractLong(JsonObject root, final String memberName) {
    return root.get(memberName).getAsLong();
  }

  protected final String extractIdenFromRdfResource(JsonObject root, final String memberName) {
    final String rdf = extractRdfResourceUrl(root, memberName);
    if (rdf != null) {
      return rdf.substring(rdf.lastIndexOf('/') + 1);
    }
    return null;
  }

  protected String extractRdf(JsonObject root) {
    return extractString(root, "rdf:resource");
  }
}
