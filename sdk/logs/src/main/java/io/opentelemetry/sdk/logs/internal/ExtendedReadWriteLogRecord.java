/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.sdk.logs.ReadWriteLogRecord;
import io.opentelemetry.sdk.logs.data.internal.ExtendedLogRecordData;
import javax.annotation.Nullable;

/**
 * A collection of configuration options which define the behavior of a {@link
 * io.opentelemetry.api.logs.Logger}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
@SuppressWarnings("deprecation")
public interface ExtendedReadWriteLogRecord extends ReadWriteLogRecord {

  /**
   * Sets an attribute on the log record. If the log record previously contained a mapping for the
   * key, the old value is replaced by the specified value.
   *
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   */
  <T> ExtendedReadWriteLogRecord setAttribute(ExtendedAttributeKey<T> key, T value);

  /**
   * Sets attributes to the {@link ReadWriteLogRecord}. If the {@link ReadWriteLogRecord} previously
   * contained a mapping for any of the keys, the old values are replaced by the specified values.
   *
   * @param extendedAttributes the attributes
   * @return this.
   */
  @SuppressWarnings("unchecked")
  default ExtendedReadWriteLogRecord setAllAttributes(ExtendedAttributes extendedAttributes) {
    if (extendedAttributes == null || extendedAttributes.isEmpty()) {
      return this;
    }
    extendedAttributes.forEach(
        (attributeKey, value) ->
            this.setAttribute((ExtendedAttributeKey<Object>) attributeKey, value));
    return this;
  }

  /** Return an immutable {@link ExtendedLogRecordData} instance representing this log record. */
  @Override
  ExtendedLogRecordData toLogRecordData();

  /**
   * Returns the value of a given attribute if it exists. This is the equivalent of calling
   * getAttributes().get(key)
   */
  @Nullable
  <T> T getAttribute(ExtendedAttributeKey<T> key);

  /** Returns the attributes for this log, or {@link ExtendedAttributes#empty()} if unset. */
  ExtendedAttributes getExtendedAttributes();
}
