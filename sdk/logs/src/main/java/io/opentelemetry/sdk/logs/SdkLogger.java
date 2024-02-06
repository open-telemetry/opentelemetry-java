/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;

/** SDK implementation of {@link Logger}. */
final class SdkLogger implements Logger {

  private static final Logger NOOP_LOGGER = LoggerProvider.noop().get("noop");

  private final LoggerSharedState loggerSharedState;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final LoggerConfig loggerConfig;

  SdkLogger(
      LoggerSharedState loggerSharedState, InstrumentationScopeInfo instrumentationScopeInfo) {
    this.loggerSharedState = loggerSharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.loggerConfig = loggerSharedState.getLoggerConfig(instrumentationScopeInfo);
  }

  @Override
  public LogRecordBuilder logRecordBuilder() {
    if (!loggerConfig.isEnabled()) {
      return NOOP_LOGGER.logRecordBuilder();
    }
    return new SdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo);
  }

  // VisibleForTesting
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }
}
