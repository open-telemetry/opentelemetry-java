/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogBuilder;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogRecord;

class SdkLogSink implements LogSink {

  private final LogSinkSharedState logSinkSharedState;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  SdkLogSink(
      LogSinkSharedState logSinkSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.logSinkSharedState = logSinkSharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  @Override
  public void offer(LogData logData) {
    logSinkSharedState.getActiveLogProcessor().process(logData);
  }

  @Override
  public LogBuilder builder() {
    return LogRecord.builder(logSinkSharedState.getResource(), instrumentationLibraryInfo);
  }
}
