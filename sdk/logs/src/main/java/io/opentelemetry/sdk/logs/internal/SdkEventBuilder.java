/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.events.EventBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.extension.incubator.logs.AnyValue;
import io.opentelemetry.extension.incubator.logs.ExtendedLogRecordBuilder;
import io.opentelemetry.sdk.common.Clock;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class SdkEventBuilder implements EventBuilder {
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
  public EventBuilder setPayload(AnyValue<?> payload) {
    ((ExtendedLogRecordBuilder) logRecordBuilder).setBody(payload);
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
    long now = clock.now();
    logRecordBuilder.setObservedTimestamp(now, TimeUnit.NANOSECONDS);
    if (!hasTimestamp) {
      logRecordBuilder.setTimestamp(now, TimeUnit.NANOSECONDS);
    }
    SdkEventEmitterProvider.addEventName(logRecordBuilder, eventName);
    logRecordBuilder.emit();
  }
}
