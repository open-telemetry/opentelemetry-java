/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.extension.incubator.logs.AnyValue;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class DefaultEventEmitter implements EventEmitter {

  private static final EventEmitter INSTANCE = new DefaultEventEmitter();

  private DefaultEventEmitter() {}

  static EventEmitter getInstance() {
    return INSTANCE;
  }

  @Override
  public void emit(String eventName) {}

  @Override
  public void emit(String eventName, AnyValue<?> payload) {}

  @Override
  public EventBuilder builder(String eventName) {
    return NoOpEventBuilder.INSTANCE;
  }

  private static class NoOpEventBuilder implements EventBuilder {

    public static final EventBuilder INSTANCE = new NoOpEventBuilder();

    @Override
    public EventBuilder setPayload(AnyValue<?> payload) {
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
