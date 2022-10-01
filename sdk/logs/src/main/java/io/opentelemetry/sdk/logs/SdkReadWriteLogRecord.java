/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.internal.AttributesMap;
import io.opentelemetry.sdk.logs.data.Body;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
class SdkReadWriteLogRecord implements ReadWriteLogRecord {

  private final LogLimits logLimits;
  private final Resource resource;
  private final InstrumentationScopeInfo instrumentationScopeInfo;
  private final long epochNanos;
  private final SpanContext spanContext;
  private final Severity severity;
  @Nullable private final String severityText;
  private final Body body;
  private final Object lock = new Object();

  @GuardedBy("lock")
  @Nullable
  private AttributesMap attributes;

  private SdkReadWriteLogRecord(
      LogLimits logLimits,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long epochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      Body body,
      @Nullable AttributesMap attributes) {
    this.logLimits = logLimits;
    this.resource = resource;
    this.instrumentationScopeInfo = instrumentationScopeInfo;
    this.epochNanos = epochNanos;
    this.spanContext = spanContext;
    this.severity = severity;
    this.severityText = severityText;
    this.body = body;
    this.attributes = attributes;
  }

  /** Create the log record with the given configuration. */
  static SdkReadWriteLogRecord create(
      LogLimits logLimits,
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      long epochNanos,
      SpanContext spanContext,
      Severity severity,
      @Nullable String severityText,
      Body body,
      @Nullable AttributesMap attributes) {
    return new SdkReadWriteLogRecord(
        logLimits,
        resource,
        instrumentationScopeInfo,
        epochNanos,
        spanContext,
        severity,
        severityText,
        body,
        attributes);
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
          epochNanos,
          spanContext,
          severity,
          severityText,
          body,
          getImmutableAttributes(),
          attributes == null ? 0 : attributes.getTotalAddedValues());
    }
  }
}
