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
package com.googlesource.gerrit.plugins.hooks.rtc.api;

import com.google.gson.Gson;
import com.googlesource.gerrit.plugins.hooks.rtc.network.Transport;

public class RtcObject {
  private String rdf;
  private String etag;
  
  public RtcObject() {
    this(null, null);
  }
  
  protected RtcObject(String rdf) {
    this(rdf, Transport.etag.get());
  }

  protected RtcObject(String rdf, String etag) {
    super();
    this.rdf = rdf;
    this.etag = etag;
  }

  public String getRdf() {
    return rdf;
  }
  
  public String getEtag() {
    return etag;
  }
  
  public String toString() {
    return new Gson().toJson(this);
  }
}
