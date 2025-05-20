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
  private static final boolean INCUBATOR_AVAILABLE;

  static {
    boolean incubatorAvailable = false;
    try {
      Class.forName("io.opentelemetry.api.incubator.logs.ExtendedDefaultLoggerProvider");
      incubatorAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    }
    INCUBATOR_AVAILABLE = incubatorAvailable;
  }

  private final LoggerSharedState loggerSharedState;
  private final InstrumentationScopeInfo instrumentationScopeInfo;

  // deliberately not volatile because of performance concerns
  // - which means its eventually consistent
  protected boolean loggerEnabled;

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
    return INCUBATOR_AVAILABLE
        ? IncubatingUtil.createExtendedLogger(sharedState, instrumentationScopeInfo, loggerConfig)
        : new SdkLogger(sharedState, instrumentationScopeInfo, loggerConfig);
  }

  @Override
  public LogRecordBuilder logRecordBuilder() {
    if (loggerEnabled) {
      return INCUBATOR_AVAILABLE
          ? IncubatingUtil.createExtendedLogRecordBuilder(
              loggerSharedState, instrumentationScopeInfo)
          : new SdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo);
    }
    return NOOP_LOGGER.logRecordBuilder();
  }

  // VisibleForTesting
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  public boolean isEnabled() {
    return loggerEnabled;
  }

  void updateLoggerConfig(LoggerConfig loggerConfig) {
    loggerEnabled = loggerConfig.isEnabled();
  }
}
