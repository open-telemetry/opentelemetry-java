/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class DefaultLogger implements Logger {

  private static final Logger INSTANCE = new DefaultLogger();
  private static final LogRecordBuilder NOOP_LOG_RECORD_BUILDER = new NoopLogRecordBuilder();

  private DefaultLogger() {}

  static Logger getInstance() {
    return INSTANCE;
  }

  @Override
  public LogRecordBuilder logRecordBuilder() {
    return NOOP_LOG_RECORD_BUILDER;
  }

  private static final class NoopLogRecordBuilder implements LogRecordBuilder {

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
