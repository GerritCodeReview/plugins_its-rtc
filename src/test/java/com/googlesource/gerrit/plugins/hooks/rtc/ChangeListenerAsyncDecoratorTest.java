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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.gerrit.common.EventListener;
import com.google.gerrit.server.events.ChangeEvent;

import com.googlesource.gerrit.plugins.hooks.rtc.filters.ChangeListenerAsyncDecorator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

@RunWith(MockitoJUnitRunner.class)
public class ChangeListenerAsyncDecoratorTest {

  @Mock
  EventListener listener;
  @Mock
  ChangeEvent event;
  @Mock
  ExecutorService executor;
  @Mock
  ExecutorService immediateExecutor;

  ChangeListenerAsyncDecorator<EventListener> asyncListener;

  @Before
  public void setUp() {
    when(immediateExecutor.submit(any(Runnable.class))).thenAnswer(
        new Answer<Future<?>>() {

          @Override
          public Future<?> answer(InvocationOnMock invocation) throws Throwable {
            Runnable task = (Runnable) invocation.getArguments()[0];
            task.run();
            return null;

          }
        });
    asyncListener =
        new ChangeListenerAsyncDecorator<EventListener>(listener, executor);
  }

  @Test
  public void testQueueShouldBeEmptyWhenCreated() {
    assertTrue(asyncListener.getQueue().isEmpty());
  }

  @Test
  public void testQueueShouldNotBeEmptyWhenOneEventSubmitted() {
    asyncListener.onEvent(event);
    assertFalse(asyncListener.getQueue().isEmpty());
  }

  @Test
  public void testChangeEventShouldBeQueuedWhenSubmitted() {
    asyncListener.onEvent(event);
    assertEquals(event, asyncListener.getQueue().peek());
  }

  @Test
  public void testChangeEventShouldBeSentToExecutor() {
    asyncListener.onEvent(event);
    verify(executor).submit(
        any(ChangeListenerAsyncDecorator.ChangeRunner.class));
  }

  @Test
  public void testChangeEventShouldBePropagatedToListenerWhenImmediatelyExecuted() {
    asyncListener =
        new ChangeListenerAsyncDecorator<>(listener, immediateExecutor);
    asyncListener.onEvent(event);
    verify(listener).onEvent(event);
    assertTrue(asyncListener.getQueue().isEmpty());
  }

  @Test
  public void testChangeEventShouldStayInQueueWhenExecutionFailed() {
    asyncListener =
        new ChangeListenerAsyncDecorator<>(listener, immediateExecutor);
    doThrow(new IllegalArgumentException()).when(listener).onEvent(
        any(ChangeEvent.class));

    asyncListener.onEvent(event);
    verify(listener).onEvent(event);
    assertFalse(asyncListener.getQueue().isEmpty());
  }

  @Test
  public void testChangeShouldProcessAllPreviouslyFailedEventsInQueue() {
    asyncListener =
        new ChangeListenerAsyncDecorator<>(listener, immediateExecutor);

    doThrow(new IllegalArgumentException()).when(listener).onEvent(
        any(ChangeEvent.class));
    asyncListener.onEvent(event);
    verify(listener).onEvent(event);
    assertFalse(asyncListener.getQueue().isEmpty());

    doNothing().when(listener).onEvent(any(ChangeEvent.class));
    asyncListener.onEvent(event);
    verify(listener, times(3)).onEvent(event);

    assertTrue(asyncListener.getQueue().isEmpty());
  }

}
