/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link LogEmitter} is the entry point into a log pipeline. Log emitters accept {@link
 * LogRecord}, and after associating them with a {@link Resource} and {@link
 * InstrumentationLibraryInfo}, pushes them to downstream {@link LogProcessor#emit(LogData)}.
 */
@ThreadSafe
public interface LogEmitter {

  /**
   * Emit a log record. Associates the log with a {@link Resource} and {@link
   * InstrumentationLibraryInfo}, and pushes it to {@link LogProcessor#emit(LogData)}.
   *
   * @param logRecord the log record
   */
  void emit(LogRecord logRecord);
}
