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

  private static final Logger INSTANCE_WITH_DOMAIN = new DefaultLogger(/* hasDomain= */ true);
  private static final Logger INSTANCE_NO_DOMAIN = new DefaultLogger(/* hasDomain= */ false);

  private static final EventBuilder NOOP_LOG_RECORD_BUILDER = new NoopLogRecordBuilder();

  private final boolean hasDomain;

  private DefaultLogger(boolean hasDomain) {
    this.hasDomain = hasDomain;
  }

  static Logger getInstance(boolean hasDomain) {
    return hasDomain ? INSTANCE_WITH_DOMAIN : INSTANCE_NO_DOMAIN;
  }

  @Override
  public EventBuilder eventBuilder(String name) {
    if (!hasDomain) {
      throw new IllegalStateException(
          "Cannot emit event from Logger without event domain. Please use LoggerBuilder#setEventDomain(String) when obtaining Logger.");
    }
    return NOOP_LOG_RECORD_BUILDER;
  }

  @Override
  public LogRecordBuilder logRecordBuilder() {
    return NOOP_LOG_RECORD_BUILDER;
  }

  private static final class NoopLogRecordBuilder implements EventBuilder {

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
