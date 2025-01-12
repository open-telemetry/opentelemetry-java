/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class ExtendedDefaultLogger implements ExtendedLogger {

  private static final Logger INSTANCE = new ExtendedDefaultLogger();
  private static final ExtendedLogRecordBuilder NOOP_LOG_RECORD_BUILDER =
      new NoopExtendedLogRecordBuilder();

  private ExtendedDefaultLogger() {}

  static Logger getNoop() {
    return INSTANCE;
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
    public LogRecordBuilder setTimestamp(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public LogRecordBuilder setTimestamp(Instant instant) {
      return this;
    }

    @Override
    public LogRecordBuilder setObservedTimestamp(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public LogRecordBuilder setObservedTimestamp(Instant instant) {
      return this;
    }

    @Override
    public LogRecordBuilder setContext(Context context) {
      return this;
    }

    @Override
    public LogRecordBuilder setSeverity(Severity severity) {
      return this;
    }

    @Override
    public LogRecordBuilder setSeverityText(String severityText) {
      return this;
    }

    @Override
    public LogRecordBuilder setBody(String body) {
      return this;
    }

    @Override
    public LogRecordBuilder setBody(Value<?> body) {
      return this;
    }

    @Override
    public <T> LogRecordBuilder setAttribute(AttributeKey<T> key, T value) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
