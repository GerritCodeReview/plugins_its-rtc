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
package com.googlesource.gerrit.plugins.hooks.rtc.filters;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gerrit.common.ChangeListener;
import com.google.gerrit.server.events.ChangeEvent;

public class ChangeListenerAsyncDecorator<T extends ChangeListener> implements
    ChangeListener {
  private static final int MAX_PENDING_EVENTS = 1024;
  private static final int MAX_BATCH_SIZE = 64;
  private static final Logger log = LoggerFactory
      .getLogger(ChangeListenerAsyncDecorator.class);
  private T innerListener;
  private final LinkedBlockingQueue<ChangeEvent> queue =
      new LinkedBlockingQueue<ChangeEvent>(MAX_PENDING_EVENTS);
  private ExecutorService executor;

  public class ChangeRunner implements Runnable {
    @Override
    public void run() {
      ArrayList<ChangeEvent> failedEvents = new ArrayList<ChangeEvent>();
      for (int i = 0; !queue.isEmpty() && i < MAX_BATCH_SIZE; i++) {
        ChangeEvent event = queue.remove();
        try {
          innerListener.onChangeEvent(event);
        } catch (Throwable e) {
          log.error("Execution of event " + event.getClass().getName() + "/"
              + event.toString()
              + " FAILED\nEvent requeued for later execution", event);
          failedEvents.add(event);
        }
      }
      
      queue.addAll(failedEvents);
    }
  }

  public ChangeListenerAsyncDecorator(T innerListener, ExecutorService executor) {
    this.innerListener = innerListener;
    this.executor = executor;
  }

  @Override
  public void onChangeEvent(ChangeEvent event) {
    queue.add(event);
    executor.submit(new ChangeRunner());
  }

  public Queue<ChangeEvent> getQueue() {
    return queue;
  }
}
