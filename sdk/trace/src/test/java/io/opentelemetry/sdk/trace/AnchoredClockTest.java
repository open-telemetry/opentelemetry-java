/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.internal.TestClock;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AnchoredClock}. */
class AnchoredClockTest {
  private static final long EPOCH_NANOS = 1234_000_005_678L;
  private final TestClock testClock = TestClock.create(EPOCH_NANOS);

  @Test
  void nanoTime() {
    assertThat(testClock.now()).isEqualTo(EPOCH_NANOS);
    AnchoredClock anchoredClock = AnchoredClock.create(testClock);
    assertThat(anchoredClock.nanoTime()).isEqualTo(testClock.nanoTime());
    testClock.advanceNanos(12345);
    assertThat(anchoredClock.nanoTime()).isEqualTo(testClock.nanoTime());
  }

  @Test
  void now_PositiveIncrease() {
    AnchoredClock anchoredClock = AnchoredClock.create(testClock);
    assertThat(anchoredClock.now()).isEqualTo(testClock.now());
    testClock.advanceNanos(3210);
    assertThat(anchoredClock.now()).isEqualTo(1234_000_008_888L);
    // Initial + 1000
    testClock.advanceNanos(-2210);
    assertThat(anchoredClock.now()).isEqualTo(1234_000_006_678L);
    testClock.advanceNanos(15_999_993_322L);
    assertThat(anchoredClock.now()).isEqualTo(1250_000_000_000L);
  }

  @Test
  void now_NegativeIncrease() {
    AnchoredClock anchoredClock = AnchoredClock.create(testClock);
    assertThat(anchoredClock.now()).isEqualTo(testClock.now());
    testClock.advanceNanos(-3456);
    assertThat(anchoredClock.now()).isEqualTo(1234_000_002_222L);
    // Initial - 1000
    testClock.advanceNanos(2456);
    assertThat(anchoredClock.now()).isEqualTo(1234_000_004_678L);
    testClock.advanceNanos(-14_000_004_678L);
    assertThat(anchoredClock.now()).isEqualTo(1220_000_000_000L);
  }
}
