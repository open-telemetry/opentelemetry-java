/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.ExtendedAttributesMap;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** SDK implementation of {@link ExtendedLogRecordBuilder}. */
final class ExtendedSdkLogRecordBuilder extends SdkLogRecordBuilder
    implements ExtendedLogRecordBuilder {

  @Nullable private ExtendedAttributesMap extendedAttributes;

  ExtendedSdkLogRecordBuilder(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      SdkLogger logger) {
    super(loggerSharedState, instrumentationScopeInfo, logger);
  }

  @Override
  public ExtendedSdkLogRecordBuilder setEventName(String eventName) {
    super.setEventName(eventName);
    return this;
  }

  @Override
  public ExtendedSdkLogRecordBuilder setException(Throwable throwable) {
    if (throwable == null) {
      return this;
    }

    loggerSharedState
        .getExceptionAttributeResolver()
        .setExceptionAttributes(
            this::setAttribute,
            throwable,
            loggerSharedState.getLogLimits().getMaxAttributeValueLength());

    return this;
  }

  @Override
  public ExtendedSdkLogRecordBuilder setTimestamp(long timestamp, TimeUnit unit) {
    super.setTimestamp(timestamp, unit);
    return this;
  }

  @Override
  public ExtendedSdkLogRecordBuilder setTimestamp(Instant instant) {
    super.setTimestamp(instant);
    return this;
  }

  @Override
  public ExtendedSdkLogRecordBuilder setObservedTimestamp(long timestamp, TimeUnit unit) {
    super.setObservedTimestamp(timestamp, unit);
    return this;
  }

  @Override
  public ExtendedSdkLogRecordBuilder setObservedTimestamp(Instant instant) {
    super.setObservedTimestamp(instant);
    return this;
  }

  @Override
  public ExtendedSdkLogRecordBuilder setContext(Context context) {
    super.setContext(context);
    return this;
  }

  @Override
  public ExtendedSdkLogRecordBuilder setSeverity(Severity severity) {
    super.setSeverity(severity);
    return this;
  }

  @Override
  public ExtendedSdkLogRecordBuilder setSeverityText(String severityText) {
    super.setSeverityText(severityText);
    return this;
  }

  @Override
  public ExtendedSdkLogRecordBuilder setBody(String body) {
    super.setBody(body);
    return this;
  }

  @Override
  public ExtendedSdkLogRecordBuilder setBody(Value<?> value) {
    super.setBody(value);
    return this;
  }

  @Override
  public <T> ExtendedSdkLogRecordBuilder setAttribute(ExtendedAttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    if (this.extendedAttributes == null) {
      this.extendedAttributes =
          ExtendedAttributesMap.create(
              logLimits.getMaxNumberOfAttributes(), logLimits.getMaxAttributeValueLength());
    }
    this.extendedAttributes.put(key, value);
    return this;
  }

  @Override
  public <T> ExtendedSdkLogRecordBuilder setAttribute(AttributeKey<T> key, @Nullable T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    return setAttribute(ExtendedAttributeKey.fromAttributeKey(key), value);
  }

  @Override
  protected ReadWriteLogRecord createLogRecord(Context context, long observedTimestampEpochNanos) {
    return ExtendedSdkReadWriteLogRecord.create(
        loggerSharedState.getLogLimits(),
        loggerSharedState.getResource(),
        instrumentationScopeInfo,
        eventName,
        timestampEpochNanos,
        observedTimestampEpochNanos,
        Span.fromContext(context).getSpanContext(),
        severity,
        severityText,
        body,
        extendedAttributes);
  }
}
