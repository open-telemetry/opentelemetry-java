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

package io.opentelemetry.sdk.trace;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.propagation.BinaryTraceContext;
import io.opentelemetry.trace.propagation.HttpTraceContext;
import io.opentelemetry.trace.unsafe.ContextUtils;
import io.opentelemetry.trace.util.Samplers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TracerSdk}. */
@RunWith(JUnit4.class)
public class TracerSdkTest {
  private static final String SPAN_NAME = "span_name";
  @Mock private Span span;
  @Mock private SpanProcessor spanProcessor;
  private final TracerSdk tracer = new TracerSdk();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    tracer.addSpanProcessor(spanProcessor);
  }

  @Test
  public void defaultGetCurrentSpan() {
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  public void defaultSpanBuilder() {
    assertThat(tracer.spanBuilder(SPAN_NAME)).isInstanceOf(SpanBuilderSdk.class);
  }

  @Test
  public void defaultHttpTextFormat() {
    assertThat(tracer.getHttpTextFormat()).isInstanceOf(HttpTraceContext.class);
  }

  @Test
  public void defaultBinaryFormat() {
    assertThat(tracer.getBinaryFormat()).isInstanceOf(BinaryTraceContext.class);
  }

  @Test
  public void getCurrentSpan() {
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    Context origContext = ContextUtils.withValue(span).attach();
    // Make sure context is detached even if test fails.
    try {
      assertThat(tracer.getCurrentSpan()).isSameInstanceAs(span);
    } finally {
      Context.current().detach(origContext);
    }
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  public void withSpan_NullSpan() {
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    try (Scope ws = tracer.withSpan(null)) {
      assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    }
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  public void getCurrentSpan_WithSpan() {
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    try (Scope ws = tracer.withSpan(span)) {
      assertThat(tracer.getCurrentSpan()).isSameInstanceAs(span);
    }
    assertThat(tracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  public void updateActiveTraceConfig() {
    assertThat(tracer.getActiveTraceConfig()).isEqualTo(TraceConfig.getDefault());
    TraceConfig newConfig =
        TraceConfig.getDefault().toBuilder().setSampler(Samplers.neverSample()).build();
    tracer.updateActiveTraceConfig(newConfig);
    assertThat(tracer.getActiveTraceConfig()).isEqualTo(newConfig);
  }

  @Test
  public void shutdown() {
    try {
      tracer.shutdown();
      Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
    } finally {
      tracer.unsafeRestart();
    }
  }

  @Test
  public void shutdownTwice_OnlyFlushSpanProcessorOnce() {
    try {
      tracer.shutdown();
      Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
      tracer.shutdown(); // the second call will be ignored
      Mockito.verify(spanProcessor, Mockito.times(1)).shutdown();
    } finally {
      tracer.unsafeRestart();
    }
  }

  @Test
  public void returnNoopSpanAfterShutdown() {
    try {
      tracer.shutdown();
      Span span =
          tracer
              .spanBuilder("span")
              .setRecordEvents(true)
              .setSampler(Samplers.alwaysSample())
              .startSpan();
      assertThat(span).isInstanceOf(DefaultSpan.class);
      span.end();
    } finally {
      tracer.unsafeRestart();
    }
  }
}
