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
import io.opentelemetry.context.propagation.TraceContextFormat;
import io.opentelemetry.resources.Resource;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Span.Kind;
import io.opentelemetry.trace.SpanData;
import io.opentelemetry.trace.Status;
import io.opentelemetry.trace.unsafe.ContextUtils;
import io.opentelemetry.trace.util.Samplers;
import java.util.Collections;
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
    assertThat(tracer.getHttpTextFormat()).isInstanceOf(TraceContextFormat.class);
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
  public void recordSpanData() {
    SpanData spanData =
        SpanData.create(
            DefaultSpan.getInvalid().getContext(),
            null,
            Resource.getEmpty(),
            SPAN_NAME,
            Kind.CLIENT,
            SpanData.Timestamp.create(1, 0),
            Collections.<String, AttributeValue>emptyMap(),
            Collections.<SpanData.TimedEvent>emptyList(),
            Collections.<Link>emptyList(),
            Status.OK,
            SpanData.Timestamp.create(2, 0));
    tracer.recordSpanData(spanData);
    Mockito.verify(spanProcessor, Mockito.times(1)).onEndSync(Mockito.any(ReadableSpanData.class));
  }

  @Test
  public void updateActiveTraceConfig() {
    assertThat(tracer.getActiveTraceConfig()).isEqualTo(TraceConfig.getDefault());
    TraceConfig newConfig =
        TraceConfig.getDefault().toBuilder().setSampler(Samplers.neverSample()).build();
    tracer.updateActiveTraceConfig(newConfig);
    assertThat(tracer.getActiveTraceConfig()).isEqualTo(newConfig);
  }
}
