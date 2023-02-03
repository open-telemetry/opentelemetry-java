/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.events.EventEmitter;
import io.opentelemetry.api.events.EventEmitterBuilder;
import io.opentelemetry.api.events.EventEmitterProvider;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.api.logs.LoggerProvider;

/**
 * SDK implementation for {@link EventEmitterProvider}.
 *
 * <p>Delegates all calls to the configured {@link LoggerProvider}, and its {@link LoggerBuilder}s,
 * {@link Logger}s.
 */
public final class SdkEventEmitterProvider implements EventEmitterProvider {

  private static final String DEFAULT_EVENT_DOMAIN = "unknown";

  private final LoggerProvider delegateLoggerProvider;

  private SdkEventEmitterProvider(LoggerProvider delegateLoggerProvider) {
    this.delegateLoggerProvider = delegateLoggerProvider;
  }

  /**
   * Create a {@link SdkEventEmitterProvider} which delegates to the {@code delegateLoggerProvider}.
   */
  public static SdkEventEmitterProvider create(LoggerProvider delegateLoggerProvider) {
    return new SdkEventEmitterProvider(delegateLoggerProvider);
  }

  @Override
  public EventEmitter get(String instrumentationScopeName) {
    return eventEmitterBuilder(instrumentationScopeName)
        .setEventDomain(DEFAULT_EVENT_DOMAIN)
        .build();
  }

  @Override
  public EventEmitterBuilder eventEmitterBuilder(String instrumentationScopeName) {
    return new SdkEventEmitterBuilder(
        delegateLoggerProvider.loggerBuilder(instrumentationScopeName));
  }

  private static class SdkEventEmitterBuilder implements EventEmitterBuilder {

    private final LoggerBuilder delegateLoggerBuilder;
    private String eventDomain = DEFAULT_EVENT_DOMAIN;

    private SdkEventEmitterBuilder(LoggerBuilder delegateLoggerBuilder) {
      this.delegateLoggerBuilder = delegateLoggerBuilder;
    }

    @Override
    public EventEmitterBuilder setEventDomain(String eventDomain) {
      this.eventDomain = eventDomain;
      return this;
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
      return new SdkEventEmitter(delegateLoggerBuilder.build(), eventDomain);
    }
  }

  private static class SdkEventEmitter implements EventEmitter {

    private static final AttributeKey<String> EVENT_DOMAIN = AttributeKey.stringKey("event.domain");
    private static final AttributeKey<String> EVENT_NAME = AttributeKey.stringKey("event.name");

    private final Logger delegateLogger;
    private final String eventDomain;

    private SdkEventEmitter(Logger delegateLogger, String eventDomain) {
      this.delegateLogger = delegateLogger;
      this.eventDomain = eventDomain;
    }

    @Override
    public void emit(String eventName, Attributes attributes) {
      delegateLogger
          .logRecordBuilder()
          .setAllAttributes(attributes)
          .setAttribute(EVENT_DOMAIN, eventDomain)
          .setAttribute(EVENT_NAME, eventName)
          .emit();
    }
  }
}
