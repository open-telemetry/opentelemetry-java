/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.internal.ExtendedLogRecordData;

/**
 * A collection of configuration options which define the behavior of a {@link
 * io.opentelemetry.api.logs.Logger}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public interface ExtendedReadWriteLogRecord extends ReadWriteLogRecord {

  /**
   * Sets an attribute on the log record. If the log record previously contained a mapping for the
   * key, the old value is replaced by the specified value.
   *
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   */
  @Override
  <T> ExtendedReadWriteLogRecord setAttribute(AttributeKey<T> key, T value);

  /**
   * Sets attributes to the {@link ReadWriteLogRecord}. If the {@link ReadWriteLogRecord} previously
   * contained a mapping for any of the keys, the old values are replaced by the specified values.
   *
   * @param attributes the attributes
   * @return this.
   */
  @Override
  @SuppressWarnings("unchecked")
  default ExtendedReadWriteLogRecord setAllAttributes(Attributes attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return this;
    }
    attributes.forEach(
        (attributeKey, value) ->
            this.setAttribute((AttributeKey<Object>) attributeKey, value));
    return this;
  }

  /** Return an immutable {@link ExtendedLogRecordData} instance representing this log record. */
  @Override
  ExtendedLogRecordData toLogRecordData();
}
