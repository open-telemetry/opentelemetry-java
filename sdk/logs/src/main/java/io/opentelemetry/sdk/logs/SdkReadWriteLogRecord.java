/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeLimits;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class SdkReadWriteLogRecord implements ReadWriteLogRecord {

  private final LogLimits logLimits;
  private final Resource resource;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final long timestampEpochNanos;
  private final long observedTimestampEpochNanos;
  private final SpanContext spanContext;
  private final Severity severity;
  @Nullable private final String severityText;
  @Nullable private final Value<?> body;
  @Nullable private final String eventName;
  private final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private AttributesBuilder attributes;

  @GuardedBy("lock")
  private int totalAttributeCount;

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
      @Nullable AttributesBuilder attributes,
      int initialTotalAttributeCount,
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
    this.totalAttributeCount = initialTotalAttributeCount;
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
      @Nullable AttributesBuilder attributes,
      int initialTotalAttributeCount,
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
        initialTotalAttributeCount,
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
            Attributes.builder(
                AttributeLimits.builder()
                    .setCapacity(logLimits.getMaxNumberOfAttributes())
                    .setLengthLimit(logLimits.getMaxAttributeValueLength())
                    .build());
      }
      totalAttributeCount++;
      attributes.put(key, value);
    }
    return this;
  }

  protected Attributes getImmutableAttributes() {
    synchronized (lock) {
      return attributes == null ? Attributes.empty() : attributes.build();
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
          totalAttributeCount,
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
      return attributes == null ? null : attributes.build().get(key);
    }
  }
}
