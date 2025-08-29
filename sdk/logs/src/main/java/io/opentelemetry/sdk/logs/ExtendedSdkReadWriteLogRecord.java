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
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.logs.data.internal.ExtendedLogRecordData;
import io.opentelemetry.sdk.logs.internal.ExtendedReadWriteLogRecord;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class ExtendedSdkReadWriteLogRecord extends SdkReadWriteLogRecord
    implements ExtendedReadWriteLogRecord {

  private final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private AttributesMap attributes;

  @SuppressWarnings("unused")
  private ExtendedSdkReadWriteLogRecord(
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
      @Nullable AttributesMap attributes) {
    super(
        logLimits,
        resource,
        instrumentationScopeInfo,
        timestampEpochNanos,
        observedTimestampEpochNanos,
        spanContext,
        severity,
        severityText,
        body,
        null,
        eventName);
    this.attributes = attributes;
  }

  /** Create the extended log record with the given configuration. */
  static ExtendedSdkReadWriteLogRecord create(
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
      @Nullable AttributesMap attributes) {
    return new ExtendedSdkReadWriteLogRecord(
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
        attributes);
  }

  @Override
  public <T> ExtendedSdkReadWriteLogRecord setAttribute(AttributeKey<T> key, T value) {
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
      if (attributes == null) {
        return Attributes.empty();
      }
      return attributes.immutableCopy();
    }
  }

  @Override
  public ExtendedLogRecordData toLogRecordData() {
    synchronized (lock) {
      return ExtendedSdkLogRecordData.create(
          resource,
          instrumentationScopeInfo,
          eventName,
          timestampEpochNanos,
          observedTimestampEpochNanos,
          spanContext,
          severity,
          severityText,
          body,
          getImmutableAttributes(),
          attributes == null ? 0 : attributes.getTotalAddedValues());
    }
  }

  @Override
  public Attributes getAttributes() {
    return getImmutableAttributes();
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
