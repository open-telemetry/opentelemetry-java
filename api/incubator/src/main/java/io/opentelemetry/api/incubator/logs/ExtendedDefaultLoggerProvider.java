/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerBuilder;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.common.impl.ApiUsageLogger;

public class ExtendedDefaultLoggerProvider implements LoggerProvider {

  private static final LoggerProvider INSTANCE = new ExtendedDefaultLoggerProvider();
  private static final LoggerBuilder NOOP_BUILDER = new NoopLoggerBuilder();

  private ExtendedDefaultLoggerProvider() {}

  public static LoggerProvider getNoop() {
    return INSTANCE;
  }

  @Override
  public LoggerBuilder loggerBuilder(String instrumentationScopeName) {
    if (instrumentationScopeName == null) {
      ApiUsageLogger.logNullParam(
          ExtendedDefaultLoggerProvider.class, "loggerBuilder", "instrumentationScopeName");
    }
    return NOOP_BUILDER;
  }

  private static class NoopLoggerBuilder implements LoggerBuilder {

    @Override
    public LoggerBuilder setSchemaUrl(String schemaUrl) {
      if (schemaUrl == null) {
        ApiUsageLogger.logNullParam(
            ExtendedDefaultLoggerProvider.class, "setSchemaUrl", "schemaUrl");
      }
      return this;
    }

    @Override
    public LoggerBuilder setInstrumentationVersion(String instrumentationVersion) {
      if (instrumentationVersion == null) {
        ApiUsageLogger.logNullParam(
            ExtendedDefaultLoggerProvider.class,
            "setInstrumentationVersion",
            "instrumentationVersion");
      }
      return this;
    }

    @Override
    public Logger build() {
      return ExtendedDefaultLogger.getNoop();
    }
  }
}
