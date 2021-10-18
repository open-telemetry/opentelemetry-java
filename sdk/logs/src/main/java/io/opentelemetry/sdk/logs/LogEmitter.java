/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.logs.data.ReadableLogData;
import io.opentelemetry.sdk.logs.data.ReadableLogRecordBuilder;
import io.opentelemetry.sdk.resources.Resource;

/**
 * A {@link LogEmitter} is the entry point into a log pipeline. Log emitters accept {@link
 * ReadableLogRecordBuilder}, and after associating them with a {@link Resource} and {@link
 * InstrumentationLibraryInfo}, pushes them to downstream {@link LogProcessor#emit(LogData)}.
 */
public final class LogEmitter {

  private final LogEmitterSharedState logEmitterSharedState;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  LogEmitter(
      LogEmitterSharedState logEmitterSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.logEmitterSharedState = logEmitterSharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  /**
   * Emit a log record. Associates the log with a {@link Resource} and {@link
   * InstrumentationLibraryInfo}, and pushes it to {@link LogProcessor#emit(LogData)}.
   *
   * @param logRecord the log record
   */
  public void emit(LogRecord logRecord) {
    logEmitterSharedState
        .getActiveLogProcessor()
        .emit(
            ReadableLogData.create(
                logEmitterSharedState.getResource(), instrumentationLibraryInfo, logRecord));
  }
}
