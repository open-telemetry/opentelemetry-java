/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.trace.internal.TracerConfig;

/**
 * Utilities for interacting with {@code io.opentelemetry:opentelemetry-api-incubator}, which is not
 * guaranteed to be present on the classpath. For all methods, callers MUST first separately
 * reflectively confirm that the incubator is available on the classpath.
 */
final class IncubatingUtil {

  private IncubatingUtil() {}

  static SdkTracer createExtendedTracer(
      TracerSharedState sharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerConfig tracerConfig,
      SdkTracerMetrics tracerProviderMetrics) {
    return new ExtendedSdkTracer(
        sharedState, instrumentationScopeInfo, tracerConfig, tracerProviderMetrics);
  }

  static SdkSpanBuilder createExtendedSpanBuilder(
      String spanName,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerSharedState tracerSharedState,
      SpanLimits spanLimits,
      SdkTracerMetrics tracerProviderMetrics) {
    return new ExtendedSdkSpanBuilder(
        spanName, instrumentationScopeInfo, tracerSharedState, spanLimits, tracerProviderMetrics);
  }
}
