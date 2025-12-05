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
  private static final boolean INCUBATOR_AVAILABLE;

  static {
    boolean incubatorAvailable = false;
    try {
      Class.forName("io.opentelemetry.api.incubator.trace.ExtendedDefaultTracerProvider");
      incubatorAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    }
    INCUBATOR_AVAILABLE = incubatorAvailable;
  }

  private final TracerSharedState sharedState;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final SdkTracerMetrics tracerProviderMetrics;

  protected volatile boolean tracerEnabled;

  SdkTracer(
      TracerSharedState sharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerConfig tracerConfig,
      SdkTracerMetrics tracerProviderMetrics) {
    this.sharedState = sharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.tracerEnabled = tracerConfig.isEnabled();
    this.tracerProviderMetrics = tracerProviderMetrics;
  }

  static SdkTracer create(
      TracerSharedState sharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerConfig tracerConfig,
      SdkTracerMetrics tracerProviderMetrics) {
    return INCUBATOR_AVAILABLE
        ? IncubatingUtil.createExtendedTracer(
            sharedState, instrumentationScopeInfo, tracerConfig, tracerProviderMetrics)
        : new SdkTracer(sharedState, instrumentationScopeInfo, tracerConfig, tracerProviderMetrics);
  }

  /**
   * Note that {@link ExtendedSdkTracer#spanBuilder(String)} calls this and depends on it returning
   * {@link ExtendedSdkTracer} in all cases when the incubator is present.
   */
  @Override
  public SpanBuilder spanBuilder(String spanName) {
    if (!tracerEnabled) {
      return NOOP_TRACER.spanBuilder(spanName);
    }
    if (spanName == null || spanName.trim().isEmpty()) {
      spanName = FALLBACK_SPAN_NAME;
    }
    if (sharedState.hasBeenShutdown()) {
      return NOOP_TRACER.spanBuilder(spanName);
    }
    return INCUBATOR_AVAILABLE
        ? IncubatingUtil.createExtendedSpanBuilder(
            spanName,
            instrumentationScopeInfo,
            sharedState,
            sharedState.getSpanLimits(),
            tracerProviderMetrics)
        : new SdkSpanBuilder(
            spanName,
            instrumentationScopeInfo,
            sharedState,
            sharedState.getSpanLimits(),
            tracerProviderMetrics);
  }

  // Visible for testing
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  // Visible for testing
  boolean isEnabled() {
    return tracerEnabled;
  }

  void updateTracerConfig(TracerConfig tracerConfig) {
    this.tracerEnabled = tracerConfig.isEnabled();
  }
}
