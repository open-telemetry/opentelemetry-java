/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** SDK implementation of {@link LogRecordBuilder}. */
class SdkLogRecordBuilder implements LogRecordBuilder {

  protected final LoggerSharedState loggerSharedState;
  protected final LogLimits logLimits;
  protected final SdkLogger logger;

  protected final InstrumentationScopeInfo instrumentationScopeInfo;
  protected long timestampEpochNanos;
  protected long observedTimestampEpochNanos;
  @Nullable protected Context context;
  protected Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
  @Nullable protected String severityText;
  @Nullable protected Value<?> body;
  @Nullable protected String eventName;
  @Nullable private AttributesMap attributes;

  SdkLogRecordBuilder(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      SdkLogger logger) {
    this.loggerSharedState = loggerSharedState;
    this.logLimits = loggerSharedState.getLogLimits();
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.logger = logger;
  }

  @Override
  public SdkLogRecordBuilder setEventName(String eventName) {
    this.eventName = eventName;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setTimestamp(long timestamp, TimeUnit unit) {
    this.timestampEpochNanos = unit.toNanos(timestamp);
    return this;
  }

  @Override
  public SdkLogRecordBuilder setTimestamp(Instant instant) {
    this.timestampEpochNanos =
        TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    return this;
  }

  @Override
  public LogRecordBuilder setObservedTimestamp(long timestamp, TimeUnit unit) {
    this.observedTimestampEpochNanos = unit.toNanos(timestamp);
    return this;
  }

  @Override
  public LogRecordBuilder setObservedTimestamp(Instant instant) {
    this.observedTimestampEpochNanos =
        TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
    return this;
  }

  @Override
  public SdkLogRecordBuilder setContext(Context context) {
    this.context = context;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setSeverity(Severity severity) {
    this.severity = severity;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setSeverityText(String severityText) {
    this.severityText = severityText;
    return this;
  }

  @Override
  public SdkLogRecordBuilder setBody(String body) {
    return setBody(Value.of(body));
  }

  @Override
  public SdkLogRecordBuilder setBody(Value<?> value) {
    this.body = value;
    return this;
  }

  @Override
  public <T> SdkLogRecordBuilder setAttribute(AttributeKey<T> key, @Nullable T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    if (this.attributes == null) {
      this.attributes =
          AttributesMap.create(
              logLimits.getMaxNumberOfAttributes(), logLimits.getMaxAttributeValueLength());
    }
    this.attributes.put(key, value);
    return this;
  }

  @Override
  public void emit() {
    if (loggerSharedState.hasBeenShutdown()) {
      return;
    }
    Context context = this.context == null ? Context.current() : this.context;
    if (!logger.isEnabled(severity, context)) {
      return;
    }
    long observedTimestampEpochNanos =
        this.observedTimestampEpochNanos == 0
            ? this.loggerSharedState.getClock().now()
            : this.observedTimestampEpochNanos;
    loggerSharedState
        .getLogRecordProcessor()
        .onEmit(context, createLogRecord(context, observedTimestampEpochNanos));
  }

  protected ReadWriteLogRecord createLogRecord(Context context, long observedTimestampEpochNanos) {
    return SdkReadWriteLogRecord.create(
        loggerSharedState.getLogLimits(),
        loggerSharedState.getResource(),
        instrumentationScopeInfo,
        timestampEpochNanos,
        observedTimestampEpochNanos,
        Span.fromContext(context).getSpanContext(),
        severity,
        severityText,
        body,
        attributes,
        eventName);
  }
}
