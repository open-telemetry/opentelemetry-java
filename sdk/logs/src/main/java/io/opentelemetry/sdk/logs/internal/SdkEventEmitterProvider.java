/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.events.EventBuilder;
import io.opentelemetry.api.events.EventEmitter;
import io.opentelemetry.api.events.EventEmitterBuilder;
import io.opentelemetry.api.events.EventEmitterProvider;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.TimeUnit;

/**
 * SDK implementation for {@link EventEmitterProvider}.
 *
 * <p>Delegates all calls to the configured {@link LoggerProvider}, and its {@link LoggerBuilder}s,
 * {@link Logger}s.
 */
public final class SdkEventEmitterProvider implements EventEmitterProvider {

  static final AttributeKey<String> EVENT_NAME = AttributeKey.stringKey("event.name");

  private final LoggerProvider delegateLoggerProvider;
  private final Clock clock;

  private SdkEventEmitterProvider(LoggerProvider delegateLoggerProvider, Clock clock) {
    this.delegateLoggerProvider = delegateLoggerProvider;
    this.clock = clock;
  }

  /**
   * Create a {@link SdkEventEmitterProvider} which delegates to the {@code delegateLoggerProvider}.
   */
  public static SdkEventEmitterProvider create(LoggerProvider delegateLoggerProvider) {
    return new SdkEventEmitterProvider(delegateLoggerProvider, Clock.getDefault());
  }

  /**
   * Create a {@link SdkEventEmitterProvider} which delegates to the {@code delegateLoggerProvider}.
   */
  public static SdkEventEmitterProvider create(LoggerProvider delegateLoggerProvider, Clock clock) {
    return new SdkEventEmitterProvider(delegateLoggerProvider, clock);
  }

  @Override
  public EventEmitter get(String instrumentationScopeName) {
    return eventEmitterBuilder(instrumentationScopeName).build();
  }

  @Override
  public EventEmitterBuilder eventEmitterBuilder(String instrumentationScopeName) {
    return new SdkEventEmitterBuilder(
        clock, delegateLoggerProvider.loggerBuilder(instrumentationScopeName));
  }

  private static class SdkEventEmitterBuilder implements EventEmitterBuilder {

    private final Clock clock;
    private final LoggerBuilder delegateLoggerBuilder;

    private SdkEventEmitterBuilder(Clock clock, LoggerBuilder delegateLoggerBuilder) {
      this.clock = clock;
      this.delegateLoggerBuilder = delegateLoggerBuilder;
    }

    @Override
    public EventEmitterBuilder setSchemaUrl(String schemaUrl) {
      delegateLoggerBuilder.setSchemaUrl(schemaUrl);
      return this;
    }

    @Override
    public EventEmitterBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
      delegateLoggerBuilder.setInstrumentationVersion(instrumentationScopeVersion);
      return this;
    }

    @Override
    public EventEmitter build() {
      return new SdkEventEmitter(clock, delegateLoggerBuilder.build());
    }
  }

  private static class SdkEventEmitter implements EventEmitter {

    private final Clock clock;
    private final Logger delegateLogger;

    private SdkEventEmitter(Clock clock, Logger delegateLogger) {
      this.clock = clock;
      this.delegateLogger = delegateLogger;
    }

    @Override
    public EventBuilder builder(String eventName, Attributes attributes) {
      return new SdkEventBuilder(
          delegateLogger
              .logRecordBuilder()
              .setTimestamp(clock.now(), TimeUnit.NANOSECONDS)
              .setAllAttributes(attributes),
          eventName);
    }

    @Override
    public void emit(String eventName, Attributes attributes) {
      LogRecordBuilder logRecordBuilder =
          delegateLogger
              .logRecordBuilder()
              .setTimestamp(clock.now(), TimeUnit.NANOSECONDS)
              .setAllAttributes(attributes);
      addEventName(logRecordBuilder, eventName);
      logRecordBuilder.emit();
    }
  }

  static void addEventName(LogRecordBuilder logRecordBuilder, String eventName) {
    logRecordBuilder.setAttribute(EVENT_NAME, eventName);
  }
}
