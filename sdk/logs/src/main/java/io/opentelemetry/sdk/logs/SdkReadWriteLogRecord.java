/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.internal.AttributesMap;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class SdkReadWriteLogRecord implements ReadWriteLogRecord {

  protected final LogLimits logLimits;
  protected final Resource resource;
  protected final InstrumentationScopeInfo instrumentationScopeInfo;
  protected final long timestampEpochNanos;
  protected final long observedTimestampEpochNanos;
  protected final SpanContext spanContext;
  protected final Severity severity;
  @Nullable protected final String severityText;
  @Nullable protected final Value<?> body;
  @Nullable protected String eventName;
  private final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private AttributesMap attributes;

  protected SdkReadWriteLogRecord(
      LogLimits logLimits,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long timestampEpochNanos,
      long observedTimestampEpochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      @Nullable Value<?> body,
      @Nullable AttributesMap attributes,
      @Nullable String eventName) {
    this.logLimits = logLimits;
    this.resource = resource;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.timestampEpochNanos = timestampEpochNanos;
    this.observedTimestampEpochNanos = observedTimestampEpochNanos;
    this.spanContext = spanContext;
    this.severity = severity;
    this.severityText = severityText;
    this.body = body;
    this.eventName = eventName;
    this.attributes = attributes;
  }

  /** Create the log record with the given configuration. */
  static SdkReadWriteLogRecord create(
      LogLimits logLimits,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long timestampEpochNanos,
      long observedTimestampEpochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      @Nullable Value<?> body,
      @Nullable AttributesMap attributes,
      @Nullable String eventName) {
    return new SdkReadWriteLogRecord(
        logLimits,
        resource,
        instrumentationScopeInfo,
        timestampEpochNanos,
        observedTimestampEpochNanos,
        spanContext,
        severity,
        severityText,
        body,
        attributes,
        eventName);
  }

  @Override
  public <T> ReadWriteLogRecord setAttribute(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    synchronized (lock) {
      if (attributes == null) {
        attributes =
            AttributesMap.create(
                logLimits.getMaxNumberOfAttributes(), logLimits.getMaxAttributeValueLength());
      }
      attributes.put(key, value);
    }
    return this;
  }

  private Attributes getImmutableAttributes() {
    synchronized (lock) {
      if (attributes == null || attributes.isEmpty()) {
        return Attributes.empty();
      }
      return attributes.immutableCopy();
    }
  }

  @Override
  public LogRecordData toLogRecordData() {
    synchronized (lock) {
      return SdkLogRecordData.create(
          resource,
          instrumentationScopeInfo,
          timestampEpochNanos,
          observedTimestampEpochNanos,
          spanContext,
          severity,
          severityText,
          body,
          getImmutableAttributes(),
          attributes == null ? 0 : attributes.getTotalAddedValues(),
          eventName);
    }
  }

  @Override
  public InstrumentationScopeInfo getInstrumentationScopeInfo() {
    return instrumentationScopeInfo;
  }

  @Override
  public long getTimestampEpochNanos() {
    return timestampEpochNanos;
  }

  @Override
  public long getObservedTimestampEpochNanos() {
    return observedTimestampEpochNanos;
  }

  @Override
  public SpanContext getSpanContext() {
    return spanContext;
  }

  @Override
  public Severity getSeverity() {
    return severity;
  }

  @Nullable
  @Override
  public String getSeverityText() {
    return severityText;
  }

  @Nullable
  @Override
  public Value<?> getBodyValue() {
    return body;
  }

  @Override
  public Attributes getAttributes() {
    return getImmutableAttributes();
  }

  @Override
  @Nullable
  public String getEventName() {
    return eventName;
  }

  @Nullable
  @Override
  public <T> T getAttribute(AttributeKey<T> key) {
    synchronized (lock) {
      if (attributes == null || attributes.isEmpty()) {
        return null;
      }
      return attributes.get(key);
    }
  }
}
