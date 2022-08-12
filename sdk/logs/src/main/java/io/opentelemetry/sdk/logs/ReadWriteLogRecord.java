/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.logs.data.LogData;

/** A log record that can be read from and written to. */
public interface ReadWriteLogRecord {

  /**
   * Sets an attribute on the log record. If the log record previously contained a mapping for the
   * key, the old value is replaced by the specified value.
   *
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   */
  <T> ReadWriteLogRecord setAttribute(AttributeKey<T> key, T value);

  // TODO: add additional setters

  /** Return an immutable {@link LogData} instance representing this log record. */
  LogData toLogData();

  // TODO: add additional log record accessors. Currently, all fields can be accessed indirectly via
  // #toLogData() with at the expense of additional allocations.

}
