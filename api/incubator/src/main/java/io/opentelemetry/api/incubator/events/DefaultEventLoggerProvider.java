/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

class DefaultEventLoggerProvider implements EventLoggerProvider {

  private static final EventLoggerProvider INSTANCE = new DefaultEventLoggerProvider();
  private static final EventLoggerBuilder NOOP_EVENT_LOGGER_BUILDER = new NoopEventLoggerBuilder();

  private DefaultEventLoggerProvider() {}

  static EventLoggerProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public EventLoggerBuilder eventLoggerBuilder(String instrumentationScopeName) {
    return NOOP_EVENT_LOGGER_BUILDER;
  }

  private static class NoopEventLoggerBuilder implements EventLoggerBuilder {

    @Override
    public EventLoggerBuilder setSchemaUrl(String schemaUrl) {
      return this;
    }

    @Override
    public EventLoggerBuilder setInstrumentationVersion(String instrumentationVersion) {
      return this;
    }

    @Override
    public EventLogger build() {
      return DefaultEventLogger.getInstance();
    }
  }
}
