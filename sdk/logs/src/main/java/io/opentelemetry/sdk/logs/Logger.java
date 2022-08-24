/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link Logger} is the entry point into a log pipeline.
 *
 * <p>Obtain a {@link #logRecordBuilder()}, add properties using the setters, and emit it to
 * downstream {@link LogProcessor}(s) via {@link LogRecordBuilder#emit()}.
 */
@ThreadSafe
public interface Logger {

  /**
   * Return a {@link LogRecordBuilder} to emit a log record.
   *
   * <p>Build the log record using the {@link LogRecordBuilder} setters, and emit it to downstream
   * {@link LogProcessor}(s) via {@link LogRecordBuilder#emit()}.
   */
  LogRecordBuilder logRecordBuilder();
}
