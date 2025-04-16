/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** Extended {@link LogRecordBuilder} with experimental APIs. */
public interface ExtendedLogRecordBuilder extends LogRecordBuilder {

  // keep this class even if it is empty, since experimental methods may be added in the future.

  /** {@inheritDoc} */
  @Override
  ExtendedLogRecordBuilder setTimestamp(long timestamp, TimeUnit unit);

  /** {@inheritDoc} */
  @Override
  ExtendedLogRecordBuilder setTimestamp(Instant instant);

  /** {@inheritDoc} */
  @Override
  ExtendedLogRecordBuilder setObservedTimestamp(long timestamp, TimeUnit unit);

  /** {@inheritDoc} */
  @Override
  ExtendedLogRecordBuilder setObservedTimestamp(Instant instant);

  /** {@inheritDoc} */
  @Override
  ExtendedLogRecordBuilder setContext(Context context);

  /** {@inheritDoc} */
  @Override
  ExtendedLogRecordBuilder setSeverity(Severity severity);

  /** {@inheritDoc} */
  @Override
  ExtendedLogRecordBuilder setSeverityText(String severityText);

  /** {@inheritDoc} */
  @Override
  ExtendedLogRecordBuilder setBody(String body);

  /** {@inheritDoc} */
  @Override
  default ExtendedLogRecordBuilder setBody(Value<?> body) {
    setBody(body.asString());
    return this;
  }

  /**
   * Sets the event name, which identifies the class / type of the Event.
   *
   * <p>This name should uniquely identify the event structure (both attributes and body). A log
   * record with a non-empty event name is an Event.
   */
  @Override
  ExtendedLogRecordBuilder setEventName(String eventName);

  /**
   * {@inheritDoc}
   *
   * <p>NOTE: all standard {@link AttributeKey}-value pairs can also be represented as {@link
   * ExtendedAttributeKey}-value pairs, but not all {@link ExtendedAttributeKey}-value pairs can be
   * represented as standard {@link AttributeKey}-value pairs. From the standpoint of the emitted
   * log record, there is no difference between adding attributes using the standard or extended
   * attribute APIs.
   */
  @SuppressWarnings("unchecked")
  @Override
  default ExtendedLogRecordBuilder setAllAttributes(Attributes attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return this;
    }
    attributes.forEach(
        (attributeKey, value) -> setAttribute((AttributeKey<Object>) attributeKey, value));
    return this;
  }

  /**
   * Sets attributes. If the {@link LogRecordBuilder} previously contained a mapping for any of the
   * keys, the old values are replaced by the specified values.
   *
   * <p>NOTE: all standard {@link AttributeKey}-value pairs can also be represented as {@link
   * ExtendedAttributeKey}-value pairs, but not all {@link ExtendedAttributeKey}-value pairs can be
   * represented as standard {@link AttributeKey}-value pairs. From the standpoint of the emitted
   * log record, there is no difference between adding attributes using the standard or extended
   * attribute APIs.
   */
  @SuppressWarnings("unchecked")
  default ExtendedLogRecordBuilder setAllAttributes(ExtendedAttributes attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return this;
    }
    attributes.forEach(
        (attributeKey, value) -> setAttribute((ExtendedAttributeKey<Object>) attributeKey, value));
    return this;
  }

  /**
   * {@inheritDoc}
   *
   * <p>NOTE: all standard {@link AttributeKey}-value pairs can also be represented as {@link
   * ExtendedAttributeKey}-value pairs, but not all {@link ExtendedAttributeKey}-value pairs can be
   * represented as standard {@link AttributeKey}-value pairs. From the standpoint of the emitted
   * log record, there is no difference between adding attributes using the standard or extended
   * attribute APIs.
   */
  @Override
  <T> ExtendedLogRecordBuilder setAttribute(AttributeKey<T> key, @Nullable T value);

  /**
   * Set an attribute.
   *
   * <p>NOTE: all standard {@link AttributeKey}-value pairs can also be represented as {@link
   * ExtendedAttributeKey}-value pairs, but not all {@link ExtendedAttributeKey}-value pairs can be
   * represented as standard {@link AttributeKey}-value pairs. From the standpoint of the emitted
   * log record, there is no difference between adding attributes using the standard or extended
   * attribute APIs.
   */
  <T> ExtendedLogRecordBuilder setAttribute(ExtendedAttributeKey<T> key, T value);

  /** Set standard {@code exception.*} attributes based on the {@code throwable}. */
  ExtendedLogRecordBuilder setException(Throwable throwable);
}
