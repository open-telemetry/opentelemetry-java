/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs.internal;

import io.opentelemetry.api.incubator.events.EventBuilder;
import io.opentelemetry.api.incubator.events.EventLogger;
import io.opentelemetry.api.incubator.events.EventLoggerBuilder;
import io.opentelemetry.api.incubator.events.EventLoggerProvider;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.Clock;

/**
 * SDK implementation for {@link EventLoggerProvider}.
 *
 * <p>Delegates all calls to the configured {@link LoggerProvider}, and its {@link LoggerBuilder}s,
 * {@link Logger}s.
 */
public final class SdkEventLoggerProvider implements EventLoggerProvider {

  private static final Severity DEFAULT_SEVERITY = Severity.INFO;

  private final LoggerProvider delegateLoggerProvider;
  private final Clock clock;

  private SdkEventLoggerProvider(LoggerProvider delegateLoggerProvider, Clock clock) {
    this.delegateLoggerProvider = delegateLoggerProvider;
    this.clock = clock;
  }

  /**
   * Create a {@link SdkEventLoggerProvider} which delegates to the {@code delegateLoggerProvider}.
   */
  public static SdkEventLoggerProvider create(LoggerProvider delegateLoggerProvider) {
    return new SdkEventLoggerProvider(delegateLoggerProvider, Clock.getDefault());
  }

  /**
   * Create a {@link SdkEventLoggerProvider} which delegates to the {@code delegateLoggerProvider}.
   */
  public static SdkEventLoggerProvider create(LoggerProvider delegateLoggerProvider, Clock clock) {
    return new SdkEventLoggerProvider(delegateLoggerProvider, clock);
  }

  @Override
  public EventLogger get(String instrumentationScopeName) {
    return eventLoggerBuilder(instrumentationScopeName).build();
  }

  @Override
  public EventLoggerBuilder eventLoggerBuilder(String instrumentationScopeName) {
    return new SdkEventLoggerBuilder(
        clock, delegateLoggerProvider.loggerBuilder(instrumentationScopeName));
  }

  private static class SdkEventLoggerBuilder implements EventLoggerBuilder {

    private final Clock clock;
    private final LoggerBuilder delegateLoggerBuilder;

    private SdkEventLoggerBuilder(Clock clock, LoggerBuilder delegateLoggerBuilder) {
      this.clock = clock;
      this.delegateLoggerBuilder = delegateLoggerBuilder;
    }

    @Override
    public EventLoggerBuilder setSchemaUrl(String schemaUrl) {
      delegateLoggerBuilder.setSchemaUrl(schemaUrl);
      return this;
    }

    @Override
    public EventLoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
      delegateLoggerBuilder.setInstrumentationVersion(instrumentationScopeVersion);
      return this;
    }

    @Override
    public EventLogger build() {
      return new SdkEventLogger(clock, delegateLoggerBuilder.build());
    }
  }

  private static class SdkEventLogger implements EventLogger {

    private final Clock clock;
    private final Logger delegateLogger;

    private SdkEventLogger(Clock clock, Logger delegateLogger) {
      this.clock = clock;
      this.delegateLogger = delegateLogger;
    }

    @Override
    public EventBuilder builder(String eventName) {
      return new SdkEventBuilder(
          clock,
          delegateLogger
              .logRecordBuilder()
              .setSeverity(DEFAULT_SEVERITY)
              .setContext(Context.current()),
          eventName);
    }
  }
}
