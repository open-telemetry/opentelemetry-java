/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.logs.AbstractDefaultLoggerTest;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
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

    assertThat(logger).isInstanceOf(ExtendedLogger.class);
    ExtendedLogRecordBuilder builder = (ExtendedLogRecordBuilder) logger.logRecordBuilder();
    assertThat(builder).isInstanceOf(ExtendedLogRecordBuilder.class);
    assertThat(builder.setBody(AnyValue.of(0))).isSameAs(builder);
  }
}
