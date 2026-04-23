/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import java.util.Objects;

class DefaultLoggerProvider implements LoggerProvider {

  private static final LoggerProvider INSTANCE = new DefaultLoggerProvider();
  private static final LoggerBuilder NOOP_BUILDER = new NoopLoggerBuilder();

  private DefaultLoggerProvider() {}

  static LoggerProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public LoggerBuilder loggerBuilder(String instrumentationScopeName) {
    Objects.requireNonNull(instrumentationScopeName, "instrumentationScopeName");
    return NOOP_BUILDER;
  }

  private static class NoopLoggerBuilder implements LoggerBuilder {

    @Override
    public LoggerBuilder setSchemaUrl(String schemaUrl) {
      Objects.requireNonNull(schemaUrl, "schemaUrl");
      return this;
    }

    @Override
    public LoggerBuilder setInstrumentationVersion(String instrumentationVersion) {
      Objects.requireNonNull(instrumentationVersion, "instrumentationVersion");
      return this;
    }

    @Override
    public Logger build() {
      return DefaultLogger.getInstance();
    }
  }
}
