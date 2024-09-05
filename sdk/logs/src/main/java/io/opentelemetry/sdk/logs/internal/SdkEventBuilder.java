/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.incubator.events.EventBuilder;
import io.opentelemetry.api.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

class SdkEventBuilder implements EventBuilder {

  private static final AttributeKey<String> EVENT_NAME = AttributeKey.stringKey("event.name");

  private final Map<String, Value<?>> payload = new HashMap<>();
  private final Clock clock;
  private final LogRecordBuilder logRecordBuilder;
  private final String eventName;
  private boolean hasTimestamp = false;

  SdkEventBuilder(Clock clock, LogRecordBuilder logRecordBuilder, String eventName) {
    this.clock = clock;
    this.logRecordBuilder = logRecordBuilder;
    this.eventName = eventName;
  }

  @Override
  public EventBuilder put(String key, Value<?> value) {
    payload.put(key, value);
    return this;
  }

  @Override
  public EventBuilder setTimestamp(long timestamp, TimeUnit unit) {
    this.logRecordBuilder.setTimestamp(timestamp, unit);
    this.hasTimestamp = true;
    return this;
  }

  @Override
  public EventBuilder setTimestamp(Instant instant) {
    this.logRecordBuilder.setTimestamp(instant);
    this.hasTimestamp = true;
    return this;
  }

  @Override
  public EventBuilder setContext(Context context) {
    logRecordBuilder.setContext(context);
    return this;
  }

  @Override
  public EventBuilder setSeverity(Severity severity) {
    logRecordBuilder.setSeverity(severity);
    return this;
  }

  @Override
  public EventBuilder setAttributes(Attributes attributes) {
    logRecordBuilder.setAllAttributes(attributes);
    return this;
  }

  @Override
  public void emit() {
    if (!payload.isEmpty()) {
      ((ExtendedLogRecordBuilder) logRecordBuilder).setBody(Value.of(payload));
    }
    if (!hasTimestamp) {
      logRecordBuilder.setTimestamp(clock.now(), TimeUnit.NANOSECONDS);
    }
    logRecordBuilder.setAttribute(EVENT_NAME, eventName);
    logRecordBuilder.emit();
  }
}
