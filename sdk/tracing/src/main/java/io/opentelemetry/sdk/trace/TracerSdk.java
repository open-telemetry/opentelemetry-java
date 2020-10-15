/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.trace.DefaultTracer;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import io.opentelemetry.trace.TracingContextUtils;

/** {@link TracerSdk} is SDK implementation of {@link Tracer}. */
final class TracerSdk implements Tracer {
  private final TracerSharedState sharedState;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  TracerSdk(TracerSharedState sharedState, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.sharedState = sharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  @Override
  public Span getCurrentSpan() {
    return TracingContextUtils.getCurrentSpan();
  }

  @Override
  public Scope withSpan(Span span) {
    return TracingContextUtils.currentContextWith(span);
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

  /**
   * Returns the instrumentation library specified when creating the tracer.
   *
   * @return an instance of {@link InstrumentationLibraryInfo}
   */
  InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return instrumentationLibraryInfo;
  }
}
