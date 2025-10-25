/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;

/**
 * Utilities for interacting with {@code io.opentelemetry:opentelemetry-api-incubator}, which is not
 * guaranteed to be present on the classpath. For all methods, callers MUST first separately
 * reflectively confirm that the incubator is available on the classpath.
 */
final class IncubatingUtil {

  private IncubatingUtil() {}

  static SdkLogger createExtendedLogger(
      LoggerSharedState sharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      LoggerConfig tracerConfig) {
    return new ExtendedSdkLogger(sharedState, instrumentationScopeInfo, tracerConfig);
  }

  static SdkLogRecordBuilder createExtendedLogRecordBuilder(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      SdkLogger logger) {
    return new ExtendedSdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo, logger);
  }
}
