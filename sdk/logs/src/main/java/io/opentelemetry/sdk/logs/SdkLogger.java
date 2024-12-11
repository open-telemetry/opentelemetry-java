/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;

/** SDK implementation of {@link Logger}. */
class SdkLogger implements Logger {

  private static final Logger NOOP_LOGGER = LoggerProvider.noop().get("noop");

  private final LoggerSharedState loggerSharedState;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final boolean loggerEnabled;

  SdkLogger(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      LoggerConfig loggerConfig) {
    this.loggerSharedState = loggerSharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.loggerEnabled = loggerConfig.isEnabled();
  }

  static SdkLogger create(
      LoggerSharedState sharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      LoggerConfig loggerConfig) {
    try {
      Class.forName("io.opentelemetry.api.incubator.logs.ExtendedLogger");
      return IncubatingUtil.createIncubatingLogger(
          sharedState, instrumentationScopeInfo, loggerConfig);
    } catch (Exception e) {
      return new SdkLogger(sharedState, instrumentationScopeInfo, loggerConfig);
    }
  }

  @Override
  public LogRecordBuilder logRecordBuilder() {
    if (loggerEnabled) {
      try {
        Class.forName("io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder");
        return IncubatingUtil.createIncubatingLogRecordBuilder(
            loggerSharedState, instrumentationScopeInfo);
      } catch (Exception e) {
        return new SdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo);
      }
    }
    return NOOP_LOGGER.logRecordBuilder();
  }

  // VisibleForTesting
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }
}
