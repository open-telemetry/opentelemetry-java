/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;

final class IncubatingUtil {

  private IncubatingUtil() {}

  static SdkLogger createIncubatingLogger(
      LoggerSharedState sharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      LoggerConfig tracerConfig) {
    return new ExtendedSdkLogger(sharedState, instrumentationScopeInfo, tracerConfig);
  }

  static SdkLogRecordBuilder createIncubatingLogRecordBuilder(
      LoggerSharedState loggerSharedState, InstrumentationScopeInfo instrumentationScopeInfo) {
    return new ExtendedSdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo);
  }
}
