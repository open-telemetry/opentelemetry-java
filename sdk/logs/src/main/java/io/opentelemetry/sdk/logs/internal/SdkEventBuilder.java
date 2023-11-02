/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import static io.opentelemetry.sdk.logs.internal.SdkEventEmitterProvider.EVENT_DOMAIN;
import static io.opentelemetry.sdk.logs.internal.SdkEventEmitterProvider.EVENT_NAME;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.events.EventBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.TimeUnit;

class SdkEventBuilder implements EventBuilder {
  private final Clock clock;
  private final Logger delegateLogger;
  private final String eventDomain;
  private final String eventName;
  private final Attributes attributes;
  private long epochNanos = Long.MIN_VALUE;

  SdkEventBuilder(
      Clock clock,
      Logger delegateLogger,
      String eventDomain,
      String eventName,
      Attributes attributes) {
    this.clock = clock;
    this.delegateLogger = delegateLogger;
    this.eventDomain = eventDomain;
    this.eventName = eventName;
    this.attributes = attributes;
  }

  @Override
  public EventBuilder setTimestamp(long epochNanos) {
    this.epochNanos = epochNanos;
    return this;
  }

  @Override
  public void emit() {
    long timestamp = epochNanos == Long.MIN_VALUE ? clock.now() : epochNanos;
    delegateLogger
        .logRecordBuilder()
        .setTimestamp(timestamp, TimeUnit.NANOSECONDS)
        .setAllAttributes(attributes)
        .setAttribute(EVENT_DOMAIN, eventDomain)
        .setAttribute(EVENT_NAME, eventName)
        .emit();
  }
}
