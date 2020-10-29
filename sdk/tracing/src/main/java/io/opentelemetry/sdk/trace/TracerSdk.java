/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

/** {@link TracerSdk} is SDK implementation of {@link Tracer}. */
final class TracerSdk implements Tracer {
  private final TracerSharedState sharedState;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  TracerSdk(TracerSharedState sharedState, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.sharedState = sharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  @Override
  public Span.Builder spanBuilder(String spanName) {
    if (sharedState.isStopped()) {
      return Tracer.getDefault().spanBuilder(spanName);
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
