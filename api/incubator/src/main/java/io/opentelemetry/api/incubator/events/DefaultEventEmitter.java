/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import io.opentelemetry.api.common.Attributes;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class DefaultEventEmitter implements EventEmitter {

  private static final EventEmitter INSTANCE = new DefaultEventEmitter();

  private DefaultEventEmitter() {}

  static EventEmitter getInstance() {
    return INSTANCE;
  }

  @Override
  public void emit(String eventName, Attributes attributes) {}

  @Override
  public EventBuilder builder(String eventName, Attributes attributes) {
    return NoOpEventBuilder.INSTANCE;
  }

  private static class NoOpEventBuilder implements EventBuilder {

    public static final EventBuilder INSTANCE = new NoOpEventBuilder();

    @Override
    public EventBuilder setTimestamp(long timestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public EventBuilder setTimestamp(Instant instant) {
      return this;
    }

    @Override
    public void emit() {}
  }
}
