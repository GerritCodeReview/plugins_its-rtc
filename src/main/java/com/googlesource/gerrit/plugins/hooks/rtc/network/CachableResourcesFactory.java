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
package com.googlesource.gerrit.plugins.hooks.rtc.network;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CachableResourcesFactory {

  public final Map<Class<?>, InternalFactory<?>> factories;
  private Transport transport;
 
  public CachableResourcesFactory(Transport transport) {
    this.transport = transport;
    this.factories = new ConcurrentHashMap<Class<?>, InternalFactory<?>>();
  }
  
  @SuppressWarnings("unchecked")
  public <T> T get(String url, Class<T> clazz) throws IOException {
    return (T) getFactory(clazz).get(url);
  }

  private <T> InternalFactory<?> getFactory(Class<T> clazz) {
    InternalFactory<?> factory = factories.get(clazz);
    if (factory == null) {
      factory = new InternalFactory<>(transport, clazz);
      factories.put(clazz, factory);
    }
    return factory;
  }

  private static class InternalFactory<T> {

    private final Transport transport;
    private final Map<String, T> cache;
    private Class<T> clazz;

    private InternalFactory(Transport transport, Class<T> clazz) {
      this.transport = transport;
      this.clazz = clazz;
      this.cache = new ConcurrentHashMap<String, T>();
    }
    
    private T get(final String url) throws IOException {
      T result = cache.get(url);
      if (result == null) {
        result = streamResourceFrom(url, clazz);
        cache.put(url, result);
      }
      
      return result;
    }
    
    private T streamResourceFrom(final String url, final Class<T> clazz) throws IOException {
      return transport.get(url+".json", clazz);
    }
  }
}
