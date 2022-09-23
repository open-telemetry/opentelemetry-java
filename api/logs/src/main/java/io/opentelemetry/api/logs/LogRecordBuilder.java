/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/**
 * Used to construct and emit log records from a {@link Logger}.
 *
 * <p>Obtain a {@link Logger#logRecordBuilder()}, add properties using the setters, and emit the log
 * record by calling {@link #emit()}.
 */
public interface LogRecordBuilder {

  /** Set the epoch timestamp using the timestamp and unit. */
  LogRecordBuilder setEpoch(long timestamp, TimeUnit unit);

  /** Set the epoch timestamp using the instant. */
  LogRecordBuilder setEpoch(Instant instant);

  /** Set the context. */
  LogRecordBuilder setContext(Context context);

  /** Set the severity. */
  LogRecordBuilder setSeverity(Severity severity);

  /** Set the severity text. */
  LogRecordBuilder setSeverityText(String severityText);

  /** Set the body string. */
  LogRecordBuilder setBody(String body);

  /**
   * Sets attributes. If the {@link LogRecordBuilder} previously contained a mapping for any of the
   * keys, the old values are replaced by the specified values.
   */
  @SuppressWarnings("unchecked")
  default LogRecordBuilder setAllAttributes(Attributes attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return this;
    }
    attributes.forEach(
        (attributeKey, value) -> setAttribute((AttributeKey<Object>) attributeKey, value));
    return this;
  }

  /** Sets an attribute. */
  <T> LogRecordBuilder setAttribute(AttributeKey<T> key, T value);

  /** Emit the log record. */
  void emit();
}
