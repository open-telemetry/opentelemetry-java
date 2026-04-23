/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.common.ApiUsageLogger;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

class DefaultLogger implements Logger {

  private static final Logger INSTANCE = new DefaultLogger();
  private static final LogRecordBuilder NOOP_LOG_RECORD_BUILDER = new NoopLogRecordBuilder();

  private DefaultLogger() {}

  static Logger getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isEnabled(Severity severity, Context context) {
    return false;
  }

  @Override
  public LogRecordBuilder logRecordBuilder() {
    return NOOP_LOG_RECORD_BUILDER;
  }

  private static final class NoopLogRecordBuilder implements LogRecordBuilder {

    private NoopLogRecordBuilder() {}

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
      if (context == null) {
        ApiUsageLogger.logNullParam(LogRecordBuilder.class, "setContext", "context");
        return this;
      }
      return this;
    }

    @Override
    public LogRecordBuilder setSeverity(Severity severity) {
      if (severity == null) {
        ApiUsageLogger.logNullParam(LogRecordBuilder.class, "setSeverity", "severity");
        return this;
      }
      return this;
    }

    @Override
    public LogRecordBuilder setSeverityText(String severityText) {
      if (severityText == null) {
        ApiUsageLogger.logNullParam(LogRecordBuilder.class, "setSeverityText", "severityText");
        return this;
      }
      return this;
    }

    @Override
    public LogRecordBuilder setBody(String body) {
      if (body == null) {
        ApiUsageLogger.logNullParam(LogRecordBuilder.class, "setBody", "body");
        return this;
      }
      return this;
    }

    @Override
    public LogRecordBuilder setBody(Value<?> body) {
      if (body == null) {
        ApiUsageLogger.logNullParam(LogRecordBuilder.class, "setBody", "body");
        return this;
      }
      return this;
    }

    @Override
    public <T> LogRecordBuilder setAttribute(AttributeKey<T> key, @Nullable T value) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
