/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.common.impl.ApiUsageLogger;

class DefaultLoggerProvider implements LoggerProvider {

  private static final LoggerProvider INSTANCE = new DefaultLoggerProvider();
  private static final LoggerBuilder NOOP_BUILDER = new NoopLoggerBuilder();

  private DefaultLoggerProvider() {}

  static LoggerProvider getInstance() {
    return INSTANCE;
  }

  @Override
  public LoggerBuilder loggerBuilder(String instrumentationScopeName) {
    if (instrumentationScopeName == null) {
      ApiUsageLogger.logNullParam(
          LoggerProvider.class, "loggerBuilder", "instrumentationScopeName");
    }
    return NOOP_BUILDER;
  }

  private static class NoopLoggerBuilder implements LoggerBuilder {

    @Override
    public LoggerBuilder setSchemaUrl(String schemaUrl) {
      if (schemaUrl == null) {
        ApiUsageLogger.logNullParam(LoggerBuilder.class, "setSchemaUrl", "schemaUrl");
      }
      return this;
    }

    @Override
    public LoggerBuilder setInstrumentationVersion(String instrumentationVersion) {
      if (instrumentationVersion == null) {
        ApiUsageLogger.logNullParam(
            LoggerBuilder.class, "setInstrumentationVersion", "instrumentationVersion");
      }
      return this;
    }

    @Override
    public Logger build() {
      return DefaultLogger.getInstance();
    }
  }
}
