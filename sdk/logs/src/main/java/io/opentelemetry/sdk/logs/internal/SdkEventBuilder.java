/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.api.events.EventBuilder;
import io.opentelemetry.api.logs.LogRecordBuilder;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

class SdkEventBuilder implements EventBuilder {
  private final LogRecordBuilder logRecordBuilder;
  private final String eventDomain;
  private final String eventName;

  SdkEventBuilder(LogRecordBuilder logRecordBuilder, String eventDomain, String eventName) {
    this.logRecordBuilder = logRecordBuilder;
    this.eventDomain = eventDomain;
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
    SdkEventEmitterProvider.addEventNameAndDomain(logRecordBuilder, eventDomain, eventName);
    logRecordBuilder.emit();
  }
}
