/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.time;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Tests for {@link TestClock}. */
public final class TestClockTest {

  @Test
  void setAndGetTime() {
    TestClock clock = TestClock.create(1234);
    assertThat(clock.now()).isEqualTo(1234);
    clock.setTime(9876543210L);
    assertThat(clock.now()).isEqualTo(9876543210L);
  }

  @Test
  void advanceMillis() {
    TestClock clock = TestClock.create(1_500_000_000L);
    clock.advanceMillis(2600);
    assertThat(clock.now()).isEqualTo(4_100_000_000L);
  }

  @Test
  void measureElapsedTime() {
    TestClock clock = TestClock.create(10_000_000_001L);
    long nanos1 = clock.nanoTime();
    clock.setTime(11_000_000_005L);
    long nanos2 = clock.nanoTime();
    assertThat(nanos2 - nanos1).isEqualTo(1_000_000_004L);
  }
}
