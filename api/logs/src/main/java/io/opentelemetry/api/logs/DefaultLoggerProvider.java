/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

class DefaultLoggerProvider implements LoggerProvider {

  private static final LoggerProvider INSTANCE = new DefaultLoggerProvider();
  private static final LoggerBuilder NOOP_BUILDER_WITH_DOMAIN =
      new NoopLoggerBuilder(/* hasDomain= */ true);
  private static final LoggerBuilder NOOP_BUILDER_NO_DOMAIN =
      new NoopLoggerBuilder(/* hasDomain= */ false);

  private DefaultLoggerProvider() {}

  static LoggerProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public LoggerBuilder loggerBuilder(String instrumentationScopeName) {
    return NOOP_BUILDER_NO_DOMAIN;
  }

  private static class NoopLoggerBuilder implements LoggerBuilder {

    private final boolean hasDomain;

    private NoopLoggerBuilder(boolean hasDomain) {
      this.hasDomain = hasDomain;
    }

    @Override
    @SuppressWarnings("BuilderReturnThis")
    public LoggerBuilder setEventDomain(String eventDomain) {
      return eventDomain == null ? NOOP_BUILDER_NO_DOMAIN : NOOP_BUILDER_WITH_DOMAIN;
    }

    @Override
    public LoggerBuilder setSchemaUrl(String schemaUrl) {
      return this;
    }

    @Override
    public LoggerBuilder setInstrumentationVersion(String instrumentationVersion) {
      return this;
    }

    @Override
    public Logger build() {
      return DefaultLogger.getInstance(hasDomain);
    }
  }
}
