/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.incubator.common.ExtendedAttributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.internal.ExtendedAttributesMap;
import io.opentelemetry.sdk.logs.data.internal.ExtendedLogRecordData;
import io.opentelemetry.sdk.logs.internal.ExtendedReadWriteLogRecord;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
@SuppressWarnings("deprecation")
class ExtendedSdkReadWriteLogRecord extends SdkReadWriteLogRecord
    implements ExtendedReadWriteLogRecord {

  private final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private ExtendedAttributesMap extendedAttributes;

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
      @Nullable ExtendedAttributesMap extendedAttributes) {
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
    this.extendedAttributes = extendedAttributes;
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
      @Nullable ExtendedAttributesMap extendedAttributes) {
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
        extendedAttributes);
  }

  @Override
  public <T> ExtendedSdkReadWriteLogRecord setAttribute(AttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    return setAttribute(ExtendedAttributeKey.fromAttributeKey(key), value);
  }

  @Override
  public <T> ExtendedSdkReadWriteLogRecord setAttribute(ExtendedAttributeKey<T> key, T value) {
    if (key == null || key.getKey().isEmpty() || value == null) {
      return this;
    }
    synchronized (lock) {
      if (extendedAttributes == null) {
        extendedAttributes =
            ExtendedAttributesMap.create(
                logLimits.getMaxNumberOfAttributes(), logLimits.getMaxAttributeValueLength());
      }
      extendedAttributes.put(key, value);
    }
    return this;
  }

  private ExtendedAttributes getImmutableExtendedAttributes() {
    synchronized (lock) {
      if (extendedAttributes == null) {
        return ExtendedAttributes.empty();
      }
      return extendedAttributes.immutableCopy();
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
          getImmutableExtendedAttributes(),
          extendedAttributes == null ? 0 : extendedAttributes.getTotalAddedValues());
    }
  }

  @Override
  public Attributes getAttributes() {
    return getExtendedAttributes().asAttributes();
  }

  @Nullable
  @Override
  public <T> T getAttribute(AttributeKey<T> key) {
    return getAttribute(ExtendedAttributeKey.fromAttributeKey(key));
  }

  @Nullable
  @Override
  public <T> T getAttribute(ExtendedAttributeKey<T> key) {
    synchronized (lock) {
      if (extendedAttributes == null || extendedAttributes.isEmpty()) {
        return null;
      }
      return extendedAttributes.get(key);
    }
  }

  @Override
  public ExtendedAttributes getExtendedAttributes() {
    return getImmutableExtendedAttributes();
  }
}
