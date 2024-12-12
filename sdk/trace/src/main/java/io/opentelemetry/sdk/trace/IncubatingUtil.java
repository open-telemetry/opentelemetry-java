/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.trace.internal.TracerConfig;

final class IncubatingUtil {

  private IncubatingUtil() {}

  static SdkTracer createIncubatingTracer(
      TracerSharedState sharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerConfig tracerConfig) {
    return new ExtendedSdkTracer(sharedState, instrumentationScopeInfo, tracerConfig);
  }

  static SdkSpanBuilder createIncubatingSpanBuilder(
      String spanName,
      InstrumentationScopeInfo instrumentationScopeInfo,
      TracerSharedState tracerSharedState,
      SpanLimits spanLimits) {
    return new ExtendedSdkSpanBuilder(
        spanName, instrumentationScopeInfo, tracerSharedState, spanLimits);
  }
}
