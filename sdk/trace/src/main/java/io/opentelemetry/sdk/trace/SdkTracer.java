/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

/** {@link SdkTracer} is SDK implementation of {@link Tracer}. */
final class SdkTracer implements Tracer {
  static final String FALLBACK_SPAN_NAME = "<unspecified span name>";

  private final TracerSharedState sharedState;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  SdkTracer(TracerSharedState sharedState, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.sharedState = sharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  @Override
  public SpanBuilder spanBuilder(String spanName) {
    if (spanName == null || spanName.trim().isEmpty()) {
      spanName = FALLBACK_SPAN_NAME;
    }
    if (sharedState.hasBeenShutdown()) {
      return Tracer.getDefault().spanBuilder(spanName);
    }
    return new SdkSpanBuilder(
        spanName, instrumentationLibraryInfo, sharedState, sharedState.getActiveTraceConfig());
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
