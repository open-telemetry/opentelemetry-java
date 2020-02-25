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

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.trace.DefaultTracer;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracingContextUtils;
import io.opentelemetry.trace.propagation.BinaryTraceContext;
import io.opentelemetry.trace.propagation.HttpTraceContext;

/** {@link TracerSdk} is SDK implementation of {@link Tracer}. */
public final class TracerSdk implements Tracer {
  private static final BinaryFormat<SpanContext> BINARY_FORMAT = new BinaryTraceContext();
  private static final HttpTextFormat<SpanContext> HTTP_TEXT_FORMAT = new HttpTraceContext();
  private final TracerSharedState sharedState;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  TracerSdk(TracerSharedState sharedState, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.sharedState = sharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  @Override
  public Span getCurrentSpan() {
    return TracingContextUtils.getSpanWithDefault(Context.current());
  }

  @Override
  public Scope withSpan(Span span) {
    return TracingContextUtils.withScopedSpan(span);
  }

  @Override
  public Span.Builder spanBuilder(String spanName) {
    if (sharedState.isStopped()) {
      return DefaultTracer.getInstance().spanBuilder(spanName);
    }
    return new SpanBuilderSdk(
        spanName,
        instrumentationLibraryInfo,
        sharedState.getActiveSpanProcessor(),
        sharedState.getActiveTraceConfig(),
        sharedState.getResource(),
        sharedState.getIdsGenerator(),
        sharedState.getClock());
  }

  @Override
  public BinaryFormat<SpanContext> getBinaryFormat() {
    return BINARY_FORMAT;
  }

  @Override
  public HttpTextFormat<SpanContext> getHttpTextFormat() {
    return HTTP_TEXT_FORMAT;
  }

  /**
   * Returns the instrumentation library specified when creating the tracer using {@link
   * TracerSdkProvider}.
   *
   * @return an instance of {@link InstrumentationLibraryInfo}
   */
  InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return instrumentationLibraryInfo;
  }
}
