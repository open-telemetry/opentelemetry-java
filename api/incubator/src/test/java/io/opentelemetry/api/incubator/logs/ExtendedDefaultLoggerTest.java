/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.logs.AbstractDefaultLoggerTest;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ExtendedDefaultLoggerTest extends AbstractDefaultLoggerTest {

  @Override
  protected LoggerProvider getLoggerProvider() {
    return ExtendedDefaultLoggerProvider.getNoop();
  }

  @Override
  protected Logger getLogger() {
    return ExtendedDefaultLogger.getNoop();
  }

  @Test
  void incubatingApiIsLoaded() {
    Logger logger = LoggerProvider.noop().get("test");

    Assertions.assertThat(logger).isInstanceOf(ExtendedLogger.class);
    Assertions.assertThat(logger.logRecordBuilder()).isInstanceOf(ExtendedLogRecordBuilder.class);
  }
}
