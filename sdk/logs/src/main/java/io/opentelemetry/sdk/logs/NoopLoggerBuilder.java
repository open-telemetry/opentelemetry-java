/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.logs.data.Severity;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class NoopLoggerBuilder implements LoggerBuilder {

  private static final NoopLoggerBuilder INSTANCE = new NoopLoggerBuilder();
  private static final NoopLogger NOOP_LOGGER = new NoopLogger();

  private NoopLoggerBuilder() {}

  static LoggerBuilder getInstance() {
    return INSTANCE;
  }

  @Override
  public LoggerBuilder setSchemaUrl(String schemaUrl) {
    return this;
  }

  @Override
  public LoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    return this;
  }

  @Override
  public Logger build() {
    return NOOP_LOGGER;
  }

  private static class NoopLogger implements Logger {

    private static final NoopLogRecordBuilder NOOP_LOG_BUILDER = new NoopLogRecordBuilder();

    private NoopLogger() {}

    @Override
    public LogRecordBuilder logRecordBuilder() {
      return NOOP_LOG_BUILDER;
    }
  }

  private static class NoopLogRecordBuilder implements LogRecordBuilder {

    private NoopLogRecordBuilder() {}

    @Override
    public LogRecordBuilder setEpoch(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public LogRecordBuilder setEpoch(Instant instant) {
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
    public <T> LogRecordBuilder setAttribute(AttributeKey<T> key, T value) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
