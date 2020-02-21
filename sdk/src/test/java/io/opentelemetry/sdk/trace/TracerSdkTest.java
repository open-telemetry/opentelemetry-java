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
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.propagation.BinaryTraceContext;
import io.opentelemetry.trace.propagation.HttpTraceContext;
import io.opentelemetry.trace.propagation.TracingContextUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TracerSdk}. */
@RunWith(JUnit4.class)
// Need to suppress warnings for MustBeClosed because Android 14 does not support
// try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
public class TracerSdkTest {
  private static final String SPAN_NAME = "span_name";
  private static final String INSTRUMENTATION_LIBRARY_NAME =
      "io.opentelemetry.sdk.trace.TracerSdkTest";
  private static final String INSTRUMENTATION_LIBRARY_VERSION = "semver:0.2.0";
  private static final InstrumentationLibraryInfo instrumentationLibraryInfo =
      InstrumentationLibraryInfo.create(
          INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION);
  @Mock private Span span;
  private final TracerSdk tracer =
      TracerSdkProvider.builder()
          .build()
          .get(INSTRUMENTATION_LIBRARY_NAME, INSTRUMENTATION_LIBRARY_VERSION);

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
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
    Context origContext = TracingContextUtils.withSpan(span).attach();
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
  public void getInstrumentationLibraryInfo() {
    assertThat(tracer.getInstrumentationLibraryInfo()).isEqualTo(instrumentationLibraryInfo);
  }

  @Test
  public void propagatesInstrumentationLibraryInfoToSpan() {
    ReadableSpan readableSpan = (ReadableSpan) tracer.spanBuilder("spanName").startSpan();
    assertThat(readableSpan.getInstrumentationLibraryInfo()).isEqualTo(instrumentationLibraryInfo);
  }
}
