/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

/** SDK implementation of {@link ExtendedLogRecordBuilder}. */
final class ExtendedSdkLogRecordBuilder extends SdkLogRecordBuilder
    implements ExtendedLogRecordBuilder {

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
    super.setException(throwable);
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
  public <T> ExtendedSdkLogRecordBuilder setAttribute(AttributeKey<T> key, T value) {
    super.setAttribute(key, value);
    return this;
  }
}
