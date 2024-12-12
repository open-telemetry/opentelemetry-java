/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.trace.internal.TracerConfig;

/** {@link SdkTracer} is SDK implementation of {@link Tracer}. */
class SdkTracer implements Tracer {
  static final String FALLBACK_SPAN_NAME = "<unspecified span name>";
  private static final Tracer NOOP_TRACER = TracerProvider.noop().get("noop");

  private final TracerSharedState sharedState;
  private final InstrumentationScopeInfo instrumentationScopeInfo;

  // TODO: add dedicated API for updating scope config.
  @SuppressWarnings("FieldCanBeFinal") // For now, allow updating reflectively.
  private boolean tracerEnabled;

  SdkTracer(
      TracerSharedState sharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerConfig tracerConfig) {
    this.sharedState = sharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.tracerEnabled = tracerConfig.isEnabled();
  }

  static SdkTracer create(
      TracerSharedState sharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerConfig tracerConfig) {
    try {
      Class.forName("io.opentelemetry.api.incubator.trace.ExtendedTracer");
      return IncubatingUtil.createIncubatingTracer(
          sharedState, instrumentationScopeInfo, tracerConfig);
    } catch (Exception e) {
      return new SdkTracer(sharedState, instrumentationScopeInfo, tracerConfig);
    }
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
    try {
      Class.forName("io.opentelemetry.api.incubator.trace.ExtendedSpanBuilder");
      return IncubatingUtil.createIncubatingSpanBuilder(
          spanName, instrumentationScopeInfo, sharedState, sharedState.getSpanLimits());
    } catch (Exception e) {
      return new SdkSpanBuilder(
          spanName, instrumentationScopeInfo, sharedState, sharedState.getSpanLimits());
    }
  }

  // Visible for testing
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }
}
