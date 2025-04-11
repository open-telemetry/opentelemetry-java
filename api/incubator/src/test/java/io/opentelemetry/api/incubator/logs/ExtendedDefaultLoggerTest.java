/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.LoggerProvider;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.api.testing.internal.AbstractDefaultLoggerTest;
import io.opentelemetry.context.Context;
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
  @SuppressWarnings("deprecation") // testing deprecated code
  void incubatingApiIsLoaded() {
    Logger logger = LoggerProvider.noop().get("test");

    assertThat(logger)
        .isInstanceOfSatisfying(
            ExtendedLogger.class,
            extendedLogger -> {
              assertThat(extendedLogger.isEnabled(Severity.ERROR, Context.current())).isFalse();
              assertThat(extendedLogger.isEnabled(Severity.ERROR)).isFalse();
              assertThat(extendedLogger.isEnabled()).isFalse();
            });
    ExtendedLogRecordBuilder builder = (ExtendedLogRecordBuilder) logger.logRecordBuilder();
    assertThat(builder).isInstanceOf(ExtendedLogRecordBuilder.class);
    assertThat(builder.setBody(Value.of(0))).isSameAs(builder);
  }
}
