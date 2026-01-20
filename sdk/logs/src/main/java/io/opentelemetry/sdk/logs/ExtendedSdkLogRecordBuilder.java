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
import io.opentelemetry.sdk.internal.ExceptionAttributeResolver;
import io.opentelemetry.sdk.internal.ExtendedAttributesMap;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** SDK implementation of {@link ExtendedLogRecordBuilder}. */
final class ExtendedSdkLogRecordBuilder extends SdkLogRecordBuilder
    implements ExtendedLogRecordBuilder {

  @Nullable private ExtendedAttributesMap extendedAttributes;

  ExtendedSdkLogRecordBuilder(
      LoggerSharedState loggerSharedState, InstrumentationScopeInfo instrumentationScopeInfo) {
    super(loggerSharedState, instrumentationScopeInfo);
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
            new ExceptionAttributeSetterWithPrecedence(),
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
  public void emit() {
    if (loggerSharedState.hasBeenShutdown()) {
      return;
    }
    Context context = this.context == null ? Context.current() : this.context;
    long observedTimestampEpochNanos =
        this.observedTimestampEpochNanos == 0
            ? this.loggerSharedState.getClock().now()
            : this.observedTimestampEpochNanos;
    loggerSharedState
        .getLogRecordProcessor()
        .onEmit(
            context,
            ExtendedSdkReadWriteLogRecord.create(
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
                extendedAttributes));
  }

  /**
   * AttributeSetter that only sets attributes if they haven't already been set by the user. This
   * ensures user-set attributes take precedence over exception-derived attributes.
   */
  private class ExceptionAttributeSetterWithPrecedence
      implements ExceptionAttributeResolver.AttributeSetter {

    @Override
    public <T> void setAttribute(AttributeKey<T> key, @javax.annotation.Nullable T value) {
      if (key == null || key.getKey().isEmpty() || value == null) {
        return;
      }
      // Only set the attribute if it hasn't been set already
      if (extendedAttributes == null || extendedAttributes.get(key) == null) {
        ExtendedSdkLogRecordBuilder.this.setAttribute(key, value);
      }
    }
  }
}
