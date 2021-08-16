/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecord;

public interface LogProcessor {

  /**
   * Adds a log record for processing.
   *
   * @param record the log record.
   */
  void addLogRecord(LogRecord record);

  /** Shuts down the processor. */
  CompletableResultCode shutdown();

  /**
   * Processes all span events that have not yet been processed.
   *
   * @return result
   */
  CompletableResultCode forceFlush();
}
