/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.common.ExtendedAttributeKey;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

class ExtendedDefaultLogger implements ExtendedLogger {

  private static final Logger INSTANCE = new ExtendedDefaultLogger();
  private static final ExtendedLogRecordBuilder NOOP_LOG_RECORD_BUILDER =
      new NoopExtendedLogRecordBuilder();

  private ExtendedDefaultLogger() {}

  static Logger getNoop() {
    return INSTANCE;
  }

  @Override
  public boolean isEnabled(Severity severity, Context context) {
    return false;
  }

  @Override
  public ExtendedLogRecordBuilder logRecordBuilder() {
    return NOOP_LOG_RECORD_BUILDER;
  }

  private static final class NoopExtendedLogRecordBuilder implements ExtendedLogRecordBuilder {

    private NoopExtendedLogRecordBuilder() {}

    @Override
    public ExtendedLogRecordBuilder setEventName(String eventName) {
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setException(Throwable throwable) {
      return this;
    }

    @Override
    public <T> ExtendedLogRecordBuilder setAttribute(ExtendedAttributeKey<T> key, T value) {
      return this;
    }

    @Override
    public <T> ExtendedLogRecordBuilder setAttribute(AttributeKey<T> key, @Nullable T value) {
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setTimestamp(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setTimestamp(Instant instant) {
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setObservedTimestamp(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setObservedTimestamp(Instant instant) {
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setContext(Context context) {
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setSeverity(Severity severity) {
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setSeverityText(String severityText) {
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setBody(String body) {
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setBody(Value<?> body) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
