/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.ExtendedAttributeKey;
import io.opentelemetry.api.common.ExtendedAttributes;
import io.opentelemetry.api.common.ExtendedAttributesBuilder;
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

  // TODO: restore
  // private final LogLimits logLimits;
  private final Resource resource;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  @Nullable private final String eventName;
  private final long timestampEpochNanos;
  private final long observedTimestampEpochNanos;
  private final SpanContext spanContext;
  private final Severity severity;
  @Nullable private final String severityText;
  @Nullable private final Value<?> body;
  private final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private ExtendedAttributesBuilder attributesBuilder;

  @SuppressWarnings("unused")
  private SdkReadWriteLogRecord(
      LogLimits logLimits,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      @Nullable String eventName,
      long timestampEpochNanos,
      long observedTimestampEpochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      @Nullable Value<?> body,
      @Nullable ExtendedAttributesBuilder attributesBuilder) {
    this.resource = resource;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.eventName = eventName;
    this.timestampEpochNanos = timestampEpochNanos;
    this.observedTimestampEpochNanos = observedTimestampEpochNanos;
    this.spanContext = spanContext;
    this.severity = severity;
    this.severityText = severityText;
    this.body = body;
    this.attributesBuilder = attributesBuilder;
  }

  /** Create the log record with the given configuration. */
  static SdkReadWriteLogRecord create(
      LogLimits logLimits,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      @Nullable String eventName,
      long timestampEpochNanos,
      long observedTimestampEpochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      @Nullable Value<?> body,
      @Nullable ExtendedAttributesBuilder attributesBuilder) {
    return new SdkReadWriteLogRecord(
        logLimits,
        resource,
        instrumentationScopeInfo,
        eventName,
        timestampEpochNanos,
        observedTimestampEpochNanos,
        spanContext,
        severity,
        severityText,
        body,
        attributesBuilder);
  }

  @Override
  public <T> ReadWriteLogRecord setAttribute(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    return setAttribute(key.asExtendedAttributeKey(), value);
  }

  @Override
  public <T> ReadWriteLogRecord setAttribute(ExtendedAttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    synchronized (lock) {
      if (attributesBuilder == null) {
        attributesBuilder = ExtendedAttributes.builder();
      }
      attributesBuilder.put(key, value);
    }
    return this;
  }

  private ExtendedAttributes getImmutableAttributes() {
    synchronized (lock) {
      if (attributesBuilder == null) {
        return ExtendedAttributes.empty();
      }
      return attributesBuilder.build();
    }
  }

  @Override
  public LogRecordData toLogRecordData() {
    synchronized (lock) {
      ExtendedAttributes attributes = getImmutableAttributes();
      return SdkLogRecordData.create(
          resource,
          instrumentationScopeInfo,
          eventName,
          timestampEpochNanos,
          observedTimestampEpochNanos,
          spanContext,
          severity,
          severityText,
          body,
          attributes,
          attributes.size());
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
    return getImmutableAttributes().asAttributes();
  }

  @Nullable
  @Override
  public <T> T getAttribute(AttributeKey<T> key) {
    return getImmutableAttributes().get(key);
  }

  @Nullable
  @Override
  public <T> T getAttribute(ExtendedAttributeKey<T> key) {
    return getImmutableAttributes().get(key);
  }
}
