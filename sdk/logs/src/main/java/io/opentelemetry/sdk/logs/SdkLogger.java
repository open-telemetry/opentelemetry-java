/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;

/** SDK implementation of {@link Logger}. */
final class SdkLogger implements Logger {

  private final LoggerSharedState loggerSharedState;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final boolean includeTraceContext;

  SdkLogger(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      boolean includeTraceContext) {
    this.loggerSharedState = loggerSharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.includeTraceContext = includeTraceContext;
  }

  SdkLogger withIncludeTraceContext(boolean includeTraceContext) {
    if (this.includeTraceContext != includeTraceContext) {
      return new SdkLogger(loggerSharedState, instrumentationScopeInfo, includeTraceContext);
    }
    return this;
  }

  @Override
  public LogRecordBuilder logRecordBuilder() {
    return new SdkLogRecordBuilder(
        loggerSharedState, instrumentationScopeInfo, includeTraceContext);
  }

  // VisibleForTesting
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }
}
