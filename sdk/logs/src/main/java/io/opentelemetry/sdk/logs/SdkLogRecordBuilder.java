/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.logs.data.Body;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/** SDK implementation of {@link LogRecordBuilder}. */
final class SdkLogRecordBuilder implements LogRecordBuilder {

  private final LoggerSharedState loggerSharedState;
  private final LogLimits logLimits;

  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final boolean includeTraceContext;
  private long timestampEpochNanos;
  @Nullable private Context context;
  private Severity severity = Severity.UNDEFINED_SEVERITY_NUMBER;
  @Nullable private String severityText;
  private Body body = Body.empty();
  @Nullable private AttributesMap attributes;

  SdkLogRecordBuilder(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      boolean includeTraceContext) {
    this.loggerSharedState = loggerSharedState;
    this.logLimits = loggerSharedState.getLogLimits();
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.includeTraceContext = includeTraceContext;
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
    this.body = Body.string(body);
    return this;
  }

  @Override
  public <T> SdkLogRecordBuilder setAttribute(AttributeKey<T> key, T value) {
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
    SpanContext spanContext;
    Context context;
    if (this.context == null) {
      context = Context.current();
      spanContext =
          includeTraceContext
              ? Span.fromContext(context).getSpanContext()
              : SpanContext.getInvalid();
    } else {
      context = this.context;
      spanContext = Span.fromContext(context).getSpanContext();
    }
    loggerSharedState
        .getLogRecordProcessor()
        .onEmit(
            context,
            SdkReadWriteLogRecord.create(
                loggerSharedState.getLogLimits(),
                loggerSharedState.getResource(),
                instrumentationScopeInfo,
                this.timestampEpochNanos == 0
                    ? this.loggerSharedState.getClock().now()
                    : this.timestampEpochNanos,
                spanContext,
                severity,
                severityText,
                body,
                attributes));
  }
}
