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
import io.opentelemetry.common.impl.ApiUsageLogger;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
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
    if (severity == null) {
      ApiUsageLogger.logNullParam(Logger.class, "isEnabled", "severity");
    }
    if (context == null) {
      ApiUsageLogger.logNullParam(Logger.class, "isEnabled", "context");
    }
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
      if (eventName == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setEventName", "eventName");
      }
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setException(Throwable throwable) {
      if (throwable == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setException", "throwable");
      }
      return this;
    }

    @Override
    public <T> ExtendedLogRecordBuilder setAttribute(ExtendedAttributeKey<T> key, T value) {
      if (key == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setAttribute", "key");
      }
      return this;
    }

    @Override
    public <T> ExtendedLogRecordBuilder setAttribute(AttributeKey<T> key, @Nullable T value) {
      if (key == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setAttribute", "key");
      }
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setTimestamp(long timestamp, TimeUnit unit) {
      if (unit == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setTimestamp", "unit");
      }
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setTimestamp(Instant instant) {
      if (instant == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setTimestamp", "instant");
      }
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setObservedTimestamp(long timestamp, TimeUnit unit) {
      if (unit == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setObservedTimestamp", "unit");
      }
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setObservedTimestamp(Instant instant) {
      if (instant == null) {
        ApiUsageLogger.logNullParam(
            ExtendedLogRecordBuilder.class, "setObservedTimestamp", "instant");
      }
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setContext(Context context) {
      if (context == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setContext", "context");
      }
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setSeverity(Severity severity) {
      if (severity == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setSeverity", "severity");
      }
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setSeverityText(String severityText) {
      if (severityText == null) {
        ApiUsageLogger.logNullParam(
            ExtendedLogRecordBuilder.class, "setSeverityText", "severityText");
      }
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setBody(String body) {
      if (body == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setBody", "body");
      }
      return this;
    }

    @Override
    public ExtendedLogRecordBuilder setBody(Value<?> body) {
      if (body == null) {
        ApiUsageLogger.logNullParam(ExtendedLogRecordBuilder.class, "setBody", "body");
      }
      return this;
    }

    @Override
    public void emit() {}
  }
}
