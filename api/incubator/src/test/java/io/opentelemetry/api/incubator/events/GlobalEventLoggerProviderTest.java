/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GlobalEventLoggerProviderTest {

  @BeforeAll
  static void beforeClass() {
    GlobalEventLoggerProvider.resetForTest();
  }

  @AfterEach
  void after() {
    GlobalEventLoggerProvider.resetForTest();
  }

  @Test
  void setAndGet() {
    assertThat(GlobalEventLoggerProvider.get()).isEqualTo(EventLoggerProvider.noop());
    EventLoggerProvider eventLoggerProvider =
        instrumentationScopeName ->
            EventLoggerProvider.noop().eventLoggerBuilder(instrumentationScopeName);
    GlobalEventLoggerProvider.set(eventLoggerProvider);
    assertThat(GlobalEventLoggerProvider.get()).isEqualTo(eventLoggerProvider);
  }

  @Test
  void setThenSet() {
    GlobalEventLoggerProvider.set(
        instrumentationScopeName ->
            EventLoggerProvider.noop().eventLoggerBuilder(instrumentationScopeName));
    assertThatThrownBy(
            () ->
                GlobalEventLoggerProvider.set(
                    instrumentationScopeName ->
                        EventLoggerProvider.noop().eventLoggerBuilder(instrumentationScopeName)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalEventLoggerProvider.set has already been called")
        .hasStackTraceContaining("setThenSet");
  }
}
