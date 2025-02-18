/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.sdk.logs.data.internal.ExtendedLogRecordData;
import javax.annotation.Nullable;

/** TODO. */
public interface ExtendedReadWriteLogRecord extends ReadWriteLogRecord {

  /** TODO. */
  <T> ExtendedReadWriteLogRecord setAttribute(ExtendedAttributeKey<T> key, T value);

  /** TODO. */
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

  /** TODO. */
  @Nullable
  <T> T getAttribute(ExtendedAttributeKey<T> key);

  /** TODO. */
  ExtendedAttributes getExtendedAttributes();
}
