/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.incubator.logs.LogEventBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import javax.annotation.Nullable;

/** SDK implementation of {@link ExtendedLogRecordBuilder}. */
final class SdkLogEventBuilder extends ExtendedSdkLogRecordBuilder implements LogEventBuilder {

  SdkLogEventBuilder(
      LoggerSharedState loggerSharedState,
      InstrumentationScopeInfo instrumentationScopeInfo,
      SdkLogger logger) {
    super(loggerSharedState, instrumentationScopeInfo, logger);
  }

  @Override
  public SdkLogEventBuilder setException(Throwable throwable) {
    super.setException(throwable);
    return this;
  }

  @Override
  public SdkLogEventBuilder setContext(Context context) {
    super.setContext(context);
    return this;
  }

  @Override
  public SdkLogEventBuilder setSeverity(Severity severity) {
    super.setSeverity(severity);
    return this;
  }

  @Override
  public SdkLogEventBuilder setBody(String body) {
    super.setBody(body);
    return this;
  }

  @Override
  public SdkLogEventBuilder setBody(Value<?> value) {
    super.setBody(value);
    return this;
  }

  @Override
  public <T> SdkLogEventBuilder setAttribute(AttributeKey<T> key, @Nullable T value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public SdkLogEventBuilder setAttribute(String key, @Nullable String value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public SdkLogEventBuilder setAttribute(String key, long value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public SdkLogEventBuilder setAttribute(String key, double value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public SdkLogEventBuilder setAttribute(String key, boolean value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public SdkLogEventBuilder setAttribute(String key, int value) {
    super.setAttribute(key, value);
    return this;
  }

  @Override
  public SdkLogEventBuilder setAllAttributes(Attributes attributes) {
    super.setAllAttributes(attributes);
    return this;
  }
}
