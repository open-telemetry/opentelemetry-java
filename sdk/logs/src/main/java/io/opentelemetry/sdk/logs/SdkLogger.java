/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
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

  protected volatile boolean loggerEnabled;
  protected volatile Severity minimumSeverity;
  protected volatile boolean traceBased;

  SdkLogger(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      LoggerConfig loggerConfig) {
    this.loggerSharedState = loggerSharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.loggerEnabled = loggerConfig.isEnabled();
    this.minimumSeverity = loggerConfig.getMinimumSeverity();
    this.traceBased = loggerConfig.isTraceBased();
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
              loggerSharedState, instrumentationScopeInfo, this)
          : new SdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo, this);
    }
    return NOOP_LOGGER.logRecordBuilder();
  }

  // VisibleForTesting
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  // Visible for testing
  public boolean isEnabled(Severity severity, Context context) {
    if (!loggerEnabled) {
      return false;
    }

    if (severity != Severity.UNDEFINED_SEVERITY_NUMBER
        && severity.getSeverityNumber() < minimumSeverity.getSeverityNumber()) {
      return false;
    }

    if (traceBased) {
      SpanContext spanContext = Span.fromContext(context).getSpanContext();
      if (spanContext.isValid() && !spanContext.getTraceFlags().isSampled()) {
        return false;
      }
    }

    return true;
  }

  void updateLoggerConfig(LoggerConfig loggerConfig) {
    loggerEnabled = loggerConfig.isEnabled();
    minimumSeverity = loggerConfig.getMinimumSeverity();
    traceBased = loggerConfig.isTraceBased();
  }
}
