/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GlobalLoggerProviderTest {

  @BeforeAll
  static void beforeClass() {
    GlobalLoggerProvider.resetForTest();
  }

  @AfterEach
  void after() {
    GlobalLoggerProvider.resetForTest();
  }

  @Test
  void setAndGet() {
    assertThat(GlobalLoggerProvider.get()).isEqualTo(LoggerProvider.noop());
    LoggerProvider loggerProvider =
        instrumentationScopeName -> LoggerProvider.noop().loggerBuilder(instrumentationScopeName);
    GlobalLoggerProvider.set(loggerProvider);
    assertThat(GlobalLoggerProvider.get()).isEqualTo(loggerProvider);
  }

  @Test
  void setThenSet() {
    GlobalLoggerProvider.set(
        instrumentationScopeName -> LoggerProvider.noop().loggerBuilder(instrumentationScopeName));
    assertThatThrownBy(
            () ->
                GlobalLoggerProvider.set(
                    instrumentationScopeName ->
                        LoggerProvider.noop().loggerBuilder(instrumentationScopeName)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalLoggerProvider.set has already been called")
        .hasStackTraceContaining("setThenSet");
  }
}
