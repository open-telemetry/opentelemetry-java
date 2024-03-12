/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.api.incubator.events.EventBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class SdkEventBuilder implements EventBuilder {
  private final LogRecordBuilder logRecordBuilder;
  private final String eventName;

  SdkEventBuilder(LogRecordBuilder logRecordBuilder, String eventName) {
    this.logRecordBuilder = logRecordBuilder;
    this.eventName = eventName;
  }

  @Override
  public EventBuilder setTimestamp(long timestamp, TimeUnit unit) {
    this.logRecordBuilder.setTimestamp(timestamp, unit);
    return this;
  }

  @Override
  public EventBuilder setTimestamp(Instant instant) {
    this.logRecordBuilder.setTimestamp(instant);
    return this;
  }

  @Override
  public void emit() {
    SdkEventEmitterProvider.addEventName(logRecordBuilder, eventName);
    logRecordBuilder.emit();
  }
}
