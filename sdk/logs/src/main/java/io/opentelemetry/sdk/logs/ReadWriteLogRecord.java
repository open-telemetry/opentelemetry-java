/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.logs.data.LogRecordData;

/**
 * A log record that can be read from and written to.
 *
 * @since 1.27.0
 */
public interface ReadWriteLogRecord {

  /**
   * Sets an attribute on the log record. If the log record previously contained a mapping for the
   * key, the old value is replaced by the specified value.
   *
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   */
  <T> ReadWriteLogRecord setAttribute(AttributeKey<T> key, T value);

  // TODO: add additional setters

  /**
   * Sets attributes to the {@link ReadWriteLogRecord}. If the {@link ReadWriteLogRecord} previously
   * contained a mapping for any of the keys, the old values are replaced by the specified values.
   *
   * @param attributes the attributes
   * @return this.
   * @since 1.31.0
   */
  @SuppressWarnings("unchecked")
  default ReadWriteLogRecord setAllAttributes(Attributes attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return this;
    }
    attributes.forEach(
        (attributeKey, value) -> this.setAttribute((AttributeKey<Object>) attributeKey, value));
    return this;
  }

  /** Return an immutable {@link LogRecordData} instance representing this log record. */
  LogRecordData toLogRecordData();

  // TODO: add additional log record accessors. Currently, all fields can be accessed indirectly via
  // #toLogRecordData() at the expense of additional allocations.

}
