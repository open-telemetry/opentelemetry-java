/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.contrib.trace.export;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.sdk.trace.MultiSpanProcessor;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link DisruptorAsyncSpanProcessor}. */
@RunWith(JUnit4.class)
public class DisruptorAsyncSpanProcessorTest {
  @Mock private ReadableSpan readableSpan;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  // Simple class to use that keeps an incrementing counter. Will fail with an assertion if
  // increment is used from multiple threads, or if the stored value is different from that expected
  // by the caller.
  private static class Counter {
    private final AtomicInteger count = new AtomicInteger(0);
    private volatile long id; // stores thread ID used in first increment operation.

    private Counter() {
      id = -1;
    }

    // Increments counter by 1. Will fail in assertion if multiple different threads are used
    // (the EventQueue backend should be single-threaded).
    private void increment() {
      long tid = Thread.currentThread().getId();
      if (id == -1) {
        assertThat(count.get()).isEqualTo(0);
        id = tid;
      } else {
        assertThat(id).isEqualTo(tid);
      }
      count.incrementAndGet();
    }
  }

  // EventQueueEntry for incrementing a Counter.
  private static class IncrementSpanProcessor implements SpanProcessor {
    private final Counter counterOnStart = new Counter();
    private final Counter counterOnEnd = new Counter();
    private final Counter counterOnShutdown = new Counter();

    @Override
    public void onStart(ReadableSpan span) {
      counterOnStart.increment();
    }

    @Override
    public void onEnd(ReadableSpan span) {
      counterOnEnd.increment();
    }

    @Override
    public void shutdown() {
      counterOnShutdown.increment();
    }

    private void checkCounterOnStart(int value) {
      assertThat(counterOnStart.count.get()).isEqualTo(value);
    }

    private void checkCounterOnEnd(int value) {
      assertThat(counterOnEnd.count.get()).isEqualTo(value);
    }

    private void checkCounterOnShutdown(int value) {
      assertThat(counterOnShutdown.count.get()).isEqualTo(value);
    }
  }

  @Test
  public void incrementOnce() {
    IncrementSpanProcessor incrementSpanProcessor = new IncrementSpanProcessor();
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    incrementSpanProcessor.checkCounterOnStart(0);
    incrementSpanProcessor.checkCounterOnEnd(0);
    disruptorAsyncSpanProcessor.onStart(readableSpan);
    disruptorAsyncSpanProcessor.onEnd(readableSpan);
    disruptorAsyncSpanProcessor.shutdown();
    incrementSpanProcessor.checkCounterOnStart(1);
    incrementSpanProcessor.checkCounterOnEnd(1);
    incrementSpanProcessor.checkCounterOnShutdown(1);
  }

  @Test
  public void shutdownIsCalledOnlyOnce() {
    IncrementSpanProcessor incrementSpanProcessor = new IncrementSpanProcessor();
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    disruptorAsyncSpanProcessor.shutdown();
    disruptorAsyncSpanProcessor.shutdown();
    disruptorAsyncSpanProcessor.shutdown();
    disruptorAsyncSpanProcessor.shutdown();
    disruptorAsyncSpanProcessor.shutdown();
    incrementSpanProcessor.checkCounterOnShutdown(1);
  }

  @Test
  public void incrementAfterShutdown() {
    IncrementSpanProcessor incrementSpanProcessor = new IncrementSpanProcessor();
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    disruptorAsyncSpanProcessor.shutdown();
    disruptorAsyncSpanProcessor.onStart(readableSpan);
    disruptorAsyncSpanProcessor.onEnd(readableSpan);
    incrementSpanProcessor.checkCounterOnStart(0);
    incrementSpanProcessor.checkCounterOnEnd(0);
    disruptorAsyncSpanProcessor.shutdown();
    incrementSpanProcessor.checkCounterOnShutdown(1);
  }

  @Test
  public void incrementTenK() {
    final int tenK = 10000;
    IncrementSpanProcessor incrementSpanProcessor = new IncrementSpanProcessor();
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    for (int i = 0; i < tenK; i++) {
      disruptorAsyncSpanProcessor.onStart(readableSpan);
      disruptorAsyncSpanProcessor.onEnd(readableSpan);
    }
    disruptorAsyncSpanProcessor.shutdown();
    incrementSpanProcessor.checkCounterOnStart(tenK);
    incrementSpanProcessor.checkCounterOnEnd(tenK);
    incrementSpanProcessor.checkCounterOnShutdown(1);
  }

  @Test
  public void incrementMultiSpanProcessor() {
    IncrementSpanProcessor incrementSpanProcessor1 = new IncrementSpanProcessor();
    IncrementSpanProcessor incrementSpanProcessor2 = new IncrementSpanProcessor();
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(
                MultiSpanProcessor.create(
                    Arrays.<SpanProcessor>asList(incrementSpanProcessor1, incrementSpanProcessor2)))
            .build();
    disruptorAsyncSpanProcessor.onStart(readableSpan);
    disruptorAsyncSpanProcessor.onEnd(readableSpan);
    disruptorAsyncSpanProcessor.shutdown();
    incrementSpanProcessor1.checkCounterOnStart(1);
    incrementSpanProcessor1.checkCounterOnEnd(1);
    incrementSpanProcessor1.checkCounterOnShutdown(1);
    incrementSpanProcessor2.checkCounterOnStart(1);
    incrementSpanProcessor2.checkCounterOnEnd(1);
    incrementSpanProcessor2.checkCounterOnShutdown(1);
  }
}
