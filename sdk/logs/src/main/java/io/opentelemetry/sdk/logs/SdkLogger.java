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

  SdkLogger(
      LoggerSharedState loggerSharedState, InstrumentationScopeInfo instrumentationScopeInfo) {
    this.loggerSharedState = loggerSharedState;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
  }

  @Override
  public LogRecordBuilder logRecordBuilder() {
    return new SdkLogRecordBuilder(loggerSharedState, instrumentationScopeInfo);
  }

  // VisibleForTesting
  InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }
}
