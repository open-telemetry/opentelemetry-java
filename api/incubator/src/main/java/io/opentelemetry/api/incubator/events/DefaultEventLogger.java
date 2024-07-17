/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import io.opentelemetry.api.common.AnyValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class DefaultEventLogger implements EventLogger {

  private static final EventLogger INSTANCE = new DefaultEventLogger();

  private DefaultEventLogger() {}

  static EventLogger getInstance() {
    return INSTANCE;
  }

  @Override
  public EventBuilder builder(String eventName) {
    return NoOpEventBuilder.INSTANCE;
  }

  private static class NoOpEventBuilder implements EventBuilder {

    public static final EventBuilder INSTANCE = new NoOpEventBuilder();

    @Override
    public EventBuilder put(String key, AnyValue<?> value) {
      return this;
    }

    @Override
    public EventBuilder setTimestamp(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public EventBuilder setTimestamp(Instant instant) {
      return this;
    }

    @Override
    public EventBuilder setContext(Context context) {
      return this;
    }

    @Override
    public EventBuilder setSeverity(Severity severity) {
      return this;
    }

    @Override
    public EventBuilder setAttributes(Attributes attributes) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
