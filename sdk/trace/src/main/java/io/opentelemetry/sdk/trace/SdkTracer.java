/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.incubator.trace.ExtendedTracer;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.trace.internal.TracerConfig;

/** {@link SdkTracer} is SDK implementation of {@link Tracer}. */
final class SdkTracer implements ExtendedTracer {
  static final String FALLBACK_SPAN_NAME = "<unspecified span name>";
  private static final Tracer NOOP_TRACER = TracerProvider.noop().get("noop");

  private final TracerSharedState sharedState;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final boolean tracerEnabled;

  SdkTracer(
      TracerSharedState sharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerConfig tracerConfig) {
    this.sharedState = sharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.tracerEnabled = tracerConfig.isEnabled();
  }

  @Override
  public SpanBuilder spanBuilder(String spanName) {
    if (!tracerEnabled) {
      return NOOP_TRACER.spanBuilder(spanName);
    }
    if (spanName == null || spanName.trim().isEmpty()) {
      spanName = FALLBACK_SPAN_NAME;
    }
    if (sharedState.hasBeenShutdown()) {
      Tracer tracer = TracerProvider.noop().get(instrumentationScopeInfo.getName());
      return tracer.spanBuilder(spanName);
    }
    return new SdkSpanBuilder(
        spanName, instrumentationScopeInfo, sharedState, sharedState.getSpanLimits());
  }

  // Visible for testing
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  @Override
  public boolean isEnabled() {
    return tracerEnabled;
  }
}
