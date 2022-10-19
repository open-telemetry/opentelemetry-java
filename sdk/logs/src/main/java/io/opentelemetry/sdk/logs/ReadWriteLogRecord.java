/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.logs.data.LogRecordData;

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

  /** Return an immutable {@link LogRecordData} instance representing this log record. */
  LogRecordData toLogRecordData();

  // TODO: add additional log record accessors. Currently, all fields can be accessed indirectly via
  // #toLogRecordData() with at the expense of additional allocations.

}
