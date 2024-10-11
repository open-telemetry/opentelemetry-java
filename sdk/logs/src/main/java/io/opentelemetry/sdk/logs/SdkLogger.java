/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.incubator.logs.ExtendedLogger;
import io.opentelemetry.api.logs.EventBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.internal.LoggerConfig;

/** SDK implementation of {@link Logger}. */
final class SdkLogger implements ExtendedLogger {

  private static final Severity DEFAULT_EVENT_SEVERITY = Severity.INFO;

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

  @Override
  public LogRecordBuilder logRecordBuilder() {
    if (loggerEnabled) {
      return new SdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo);
    }
    return NOOP_LOGGER.logRecordBuilder();
  }

  @Override
  public EventBuilder eventBuilder(String eventName) {
    if (loggerEnabled) {
      return new SdkEventBuilder(
          loggerSharedState.getClock(),
          logRecordBuilder().setSeverity(DEFAULT_EVENT_SEVERITY).setContext(Context.current()),
          eventName);
    }
    return NOOP_LOGGER.eventBuilder(eventName);
  }

  // VisibleForTesting
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  @Override
  public boolean isEnabled() {
    return loggerEnabled;
  }
}
