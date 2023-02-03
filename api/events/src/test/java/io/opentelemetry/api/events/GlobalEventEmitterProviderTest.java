/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GlobalEventEmitterProviderTest {

  @BeforeAll
  static void beforeClass() {
    GlobalEventEmitterProvider.resetForTest();
  }

  @AfterEach
  void after() {
    GlobalEventEmitterProvider.resetForTest();
  }

  @Test
  void setAndGet() {
    assertThat(GlobalEventEmitterProvider.get()).isEqualTo(EventEmitterProvider.noop());
    EventEmitterProvider eventEmitterProvider =
        instrumentationScopeName ->
            EventEmitterProvider.noop().eventEmitterBuilder(instrumentationScopeName);
    GlobalEventEmitterProvider.set(eventEmitterProvider);
    assertThat(GlobalEventEmitterProvider.get()).isEqualTo(eventEmitterProvider);
  }

  @Test
  void setThenSet() {
    GlobalEventEmitterProvider.set(
        instrumentationScopeName ->
            EventEmitterProvider.noop().eventEmitterBuilder(instrumentationScopeName));
    assertThatThrownBy(
            () ->
                GlobalEventEmitterProvider.set(
                    instrumentationScopeName ->
                        EventEmitterProvider.noop().eventEmitterBuilder(instrumentationScopeName)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("GlobalEventEmitterProvider.set has already been called")
        .hasStackTraceContaining("setThenSet");
  }
}
