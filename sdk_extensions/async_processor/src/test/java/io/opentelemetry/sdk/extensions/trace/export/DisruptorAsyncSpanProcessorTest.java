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

package io.opentelemetry.sdk.extensions.trace.export;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.trace.MultiSpanProcessor;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link DisruptorAsyncSpanProcessor}. */
class DisruptorAsyncSpanProcessorTest {
  private static final boolean REQUIRED = true;
  private static final boolean NOT_REQUIRED = false;

  @Mock private ReadableSpan readableSpan;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  // EventQueueEntry for incrementing a Counter.
  private static class IncrementSpanProcessor implements SpanProcessor {
    private final AtomicInteger counterOnStart = new AtomicInteger(0);
    private final AtomicInteger counterOnEnd = new AtomicInteger(0);
    private final AtomicInteger counterEndSpans = new AtomicInteger(0);
    private final AtomicInteger counterOnShutdown = new AtomicInteger(0);
    private final AtomicInteger counterOnForceFlush = new AtomicInteger(0);
    private final AtomicInteger counterOnExportedForceFlushSpans = new AtomicInteger(0);
    private final AtomicInteger deltaExportedForceFlushSpans = new AtomicInteger(0);
    private final boolean startRequired;
    private final boolean endRequired;

    private IncrementSpanProcessor(boolean startRequired, boolean endRequired) {
      this.startRequired = startRequired;
      this.endRequired = endRequired;
    }

    @Override
    public void onStart(ReadableSpan span) {
      counterOnStart.incrementAndGet();
    }

    @Override
    public boolean isStartRequired() {
      return startRequired;
    }

    @Override
    public void onEnd(ReadableSpan span) {
      counterOnEnd.incrementAndGet();
      counterEndSpans.incrementAndGet();
    }

    @Override
    public boolean isEndRequired() {
      return endRequired;
    }

    @Override
    public void shutdown() {
      counterOnShutdown.incrementAndGet();
    }

    @Override
    public void forceFlush() {
      counterOnForceFlush.incrementAndGet();
      deltaExportedForceFlushSpans.set(counterEndSpans.getAndSet(0));
      counterOnExportedForceFlushSpans.addAndGet(deltaExportedForceFlushSpans.get());
    }

    private int getCounterOnStart() {
      return counterOnStart.get();
    }

    private int getCounterOnEnd() {
      return counterOnEnd.get();
    }

    private int getCounterOnShutdown() {
      return counterOnShutdown.get();
    }

    private int getCounterOnForceFlush() {
      return counterOnForceFlush.get();
    }

    public int getCounterOnExportedForceFlushSpans() {
      return counterOnExportedForceFlushSpans.get();
    }

    public int getDeltaExportedForceFlushSpans() {
      return deltaExportedForceFlushSpans.get();
    }
  }

  @Test
  void incrementOnce() {
    IncrementSpanProcessor incrementSpanProcessor = new IncrementSpanProcessor(REQUIRED, REQUIRED);
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    assertThat(disruptorAsyncSpanProcessor.isStartRequired()).isTrue();
    assertThat(disruptorAsyncSpanProcessor.isEndRequired()).isTrue();
    assertThat(incrementSpanProcessor.getCounterOnStart()).isEqualTo(0);
    assertThat(incrementSpanProcessor.getCounterOnEnd()).isEqualTo(0);
    disruptorAsyncSpanProcessor.onStart(readableSpan);
    disruptorAsyncSpanProcessor.onEnd(readableSpan);
    disruptorAsyncSpanProcessor.forceFlush();
    disruptorAsyncSpanProcessor.shutdown();
    assertThat(incrementSpanProcessor.getCounterOnStart()).isEqualTo(1);
    assertThat(incrementSpanProcessor.getCounterOnEnd()).isEqualTo(1);
    assertThat(incrementSpanProcessor.getCounterOnForceFlush()).isEqualTo(1);
    assertThat(incrementSpanProcessor.getCounterOnShutdown()).isEqualTo(1);
  }

  @Test
  void incrementOnce_NoStart() {
    IncrementSpanProcessor incrementSpanProcessor =
        new IncrementSpanProcessor(NOT_REQUIRED, REQUIRED);
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    assertThat(disruptorAsyncSpanProcessor.isStartRequired()).isFalse();
    assertThat(disruptorAsyncSpanProcessor.isEndRequired()).isTrue();
    assertThat(incrementSpanProcessor.getCounterOnStart()).isEqualTo(0);
    assertThat(incrementSpanProcessor.getCounterOnEnd()).isEqualTo(0);
    disruptorAsyncSpanProcessor.onStart(readableSpan);
    disruptorAsyncSpanProcessor.onEnd(readableSpan);
    disruptorAsyncSpanProcessor.forceFlush();
    disruptorAsyncSpanProcessor.shutdown();
    assertThat(incrementSpanProcessor.getCounterOnStart()).isEqualTo(0);
    assertThat(incrementSpanProcessor.getCounterOnEnd()).isEqualTo(1);
    assertThat(incrementSpanProcessor.getCounterOnForceFlush()).isEqualTo(1);
    assertThat(incrementSpanProcessor.getCounterOnShutdown()).isEqualTo(1);
  }

  @Test
  void incrementOnce_NoEnd() {
    IncrementSpanProcessor incrementSpanProcessor =
        new IncrementSpanProcessor(REQUIRED, NOT_REQUIRED);
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    assertThat(disruptorAsyncSpanProcessor.isStartRequired()).isTrue();
    assertThat(disruptorAsyncSpanProcessor.isEndRequired()).isFalse();
    assertThat(incrementSpanProcessor.getCounterOnStart()).isEqualTo(0);
    assertThat(incrementSpanProcessor.getCounterOnEnd()).isEqualTo(0);
    disruptorAsyncSpanProcessor.onStart(readableSpan);
    disruptorAsyncSpanProcessor.onEnd(readableSpan);
    disruptorAsyncSpanProcessor.forceFlush();
    disruptorAsyncSpanProcessor.shutdown();
    assertThat(incrementSpanProcessor.getCounterOnStart()).isEqualTo(1);
    assertThat(incrementSpanProcessor.getCounterOnEnd()).isEqualTo(0);
    assertThat(incrementSpanProcessor.getCounterOnForceFlush()).isEqualTo(1);
    assertThat(incrementSpanProcessor.getCounterOnShutdown()).isEqualTo(1);
  }

  @Test
  void shutdownIsCalledOnlyOnce() {
    IncrementSpanProcessor incrementSpanProcessor = new IncrementSpanProcessor(REQUIRED, REQUIRED);
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    disruptorAsyncSpanProcessor.shutdown();
    disruptorAsyncSpanProcessor.shutdown();
    disruptorAsyncSpanProcessor.shutdown();
    disruptorAsyncSpanProcessor.shutdown();
    disruptorAsyncSpanProcessor.shutdown();
    assertThat(incrementSpanProcessor.getCounterOnShutdown()).isEqualTo(1);
  }

  @Test
  void incrementAfterShutdown() {
    IncrementSpanProcessor incrementSpanProcessor = new IncrementSpanProcessor(REQUIRED, REQUIRED);
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    disruptorAsyncSpanProcessor.shutdown();
    disruptorAsyncSpanProcessor.onStart(readableSpan);
    disruptorAsyncSpanProcessor.onEnd(readableSpan);
    disruptorAsyncSpanProcessor.forceFlush();
    assertThat(incrementSpanProcessor.getCounterOnStart()).isEqualTo(0);
    assertThat(incrementSpanProcessor.getCounterOnEnd()).isEqualTo(0);
    assertThat(incrementSpanProcessor.getCounterOnForceFlush()).isEqualTo(0);
    disruptorAsyncSpanProcessor.shutdown();
    assertThat(incrementSpanProcessor.getCounterOnShutdown()).isEqualTo(1);
  }

  @Test
  void incrementTenK() {
    final int tenK = 10000;
    IncrementSpanProcessor incrementSpanProcessor = new IncrementSpanProcessor(REQUIRED, REQUIRED);
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    for (int i = 1; i <= tenK; i++) {
      disruptorAsyncSpanProcessor.onStart(readableSpan);
      disruptorAsyncSpanProcessor.onEnd(readableSpan);
      if (i % 10 == 0) {
        disruptorAsyncSpanProcessor.forceFlush();
      }
    }
    assertThat(incrementSpanProcessor.getCounterOnStart()).isEqualTo(tenK);
    assertThat(incrementSpanProcessor.getCounterOnEnd()).isEqualTo(tenK);
    assertThat(incrementSpanProcessor.getCounterOnForceFlush()).isEqualTo(tenK / 10);
    disruptorAsyncSpanProcessor.shutdown();
    assertThat(incrementSpanProcessor.getCounterOnShutdown()).isEqualTo(1);
  }

  @Test
  void incrementMultiSpanProcessor() {
    IncrementSpanProcessor incrementSpanProcessor1 = new IncrementSpanProcessor(REQUIRED, REQUIRED);
    IncrementSpanProcessor incrementSpanProcessor2 = new IncrementSpanProcessor(REQUIRED, REQUIRED);
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(
                MultiSpanProcessor.create(
                    Arrays.asList(incrementSpanProcessor1, incrementSpanProcessor2)))
            .build();
    disruptorAsyncSpanProcessor.onStart(readableSpan);
    disruptorAsyncSpanProcessor.onEnd(readableSpan);
    disruptorAsyncSpanProcessor.shutdown();
    assertThat(incrementSpanProcessor1.getCounterOnStart()).isEqualTo(1);
    assertThat(incrementSpanProcessor1.getCounterOnEnd()).isEqualTo(1);
    assertThat(incrementSpanProcessor1.getCounterOnShutdown()).isEqualTo(1);
    assertThat(incrementSpanProcessor1.getCounterOnForceFlush()).isEqualTo(0);
    assertThat(incrementSpanProcessor2.getCounterOnStart()).isEqualTo(1);
    assertThat(incrementSpanProcessor2.getCounterOnEnd()).isEqualTo(1);
    assertThat(incrementSpanProcessor2.getCounterOnShutdown()).isEqualTo(1);
    assertThat(incrementSpanProcessor2.getCounterOnForceFlush()).isEqualTo(0);
  }

  @Test
  void multipleForceFlush() {
    final int tenK = 10000;
    IncrementSpanProcessor incrementSpanProcessor = new IncrementSpanProcessor(REQUIRED, REQUIRED);
    DisruptorAsyncSpanProcessor disruptorAsyncSpanProcessor =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor).build();
    for (int i = 1; i <= tenK; i++) {
      disruptorAsyncSpanProcessor.onStart(readableSpan);
      disruptorAsyncSpanProcessor.onEnd(readableSpan);
      if (i % 100 == 0) {
        disruptorAsyncSpanProcessor.forceFlush();
        assertThat(incrementSpanProcessor.getDeltaExportedForceFlushSpans()).isEqualTo(100);
      }
    }
    disruptorAsyncSpanProcessor.shutdown();
    assertThat(incrementSpanProcessor.getCounterOnStart()).isEqualTo(tenK);
    assertThat(incrementSpanProcessor.getCounterOnEnd()).isEqualTo(tenK);
    assertThat(incrementSpanProcessor.getCounterOnExportedForceFlushSpans()).isEqualTo(tenK);
    assertThat(incrementSpanProcessor.getCounterOnShutdown()).isEqualTo(1);
  }

  abstract static class ConfigBuilderTest extends ConfigBuilder<ConfigBuilderTest> {
    public static NamingConvention getNaming() {
      return NamingConvention.DOT;
    }
  }

  @Test
  void configTest() {
    Map<String, String> options = new HashMap<>();
    options.put("otel.disruptor.blocking", "false");
    options.put("otel.disruptor.buffer.size", "1234");
    options.put("otel.disruptor.num.retries", "56");
    options.put("otel.disruptor.sleeping.time", "78");
    IncrementSpanProcessor incrementSpanProcessor = new IncrementSpanProcessor(REQUIRED, REQUIRED);
    DisruptorAsyncSpanProcessor.Builder config =
        DisruptorAsyncSpanProcessor.newBuilder(incrementSpanProcessor);
    DisruptorAsyncSpanProcessor.Builder spy = Mockito.spy(config);
    spy.fromConfigMap(options, ConfigBuilderTest.getNaming());
    Mockito.verify(spy).setBlocking(false);
    Mockito.verify(spy).setBufferSize(1234);
  }
}
