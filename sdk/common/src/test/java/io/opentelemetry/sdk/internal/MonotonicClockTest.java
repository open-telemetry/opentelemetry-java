/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MonotonicClock}. */
class MonotonicClockTest {
  private static final long EPOCH_NANOS = 1234_000_005_678L;
  private final TestClock testClock = TestClock.create(Instant.ofEpochSecond(0, EPOCH_NANOS));

  @Test
  void nanoTime() {
    assertThat(testClock.now()).isEqualTo(EPOCH_NANOS);
    MonotonicClock monotonicClock = MonotonicClock.create(testClock);
    assertThat(monotonicClock.nanoTime()).isEqualTo(testClock.nanoTime());
    testClock.advance(Duration.ofNanos(12345));
    assertThat(monotonicClock.nanoTime()).isEqualTo(testClock.nanoTime());
  }

  @Test
  void now_PositiveIncrease() {
    MonotonicClock monotonicClock = MonotonicClock.create(testClock);
    assertThat(monotonicClock.now()).isEqualTo(testClock.now());
    testClock.advance(Duration.ofNanos(3210));
    assertThat(monotonicClock.now()).isEqualTo(1234_000_008_888L);
    // Initial + 1000
    testClock.advance(Duration.ofNanos(-2210));
    assertThat(monotonicClock.now()).isEqualTo(1234_000_006_678L);
    testClock.advance(Duration.ofNanos(15_999_993_322L));
    assertThat(monotonicClock.now()).isEqualTo(1250_000_000_000L);
  }

  @Test
  void now_NegativeIncrease() {
    MonotonicClock monotonicClock = MonotonicClock.create(testClock);
    assertThat(monotonicClock.now()).isEqualTo(testClock.now());
    testClock.advance(Duration.ofNanos(-3456));
    assertThat(monotonicClock.now()).isEqualTo(1234_000_002_222L);
    // Initial - 1000
    testClock.advance(Duration.ofNanos(2456));
    assertThat(monotonicClock.now()).isEqualTo(1234_000_004_678L);
    testClock.advance(Duration.ofNanos(-14_000_004_678L));
    assertThat(monotonicClock.now()).isEqualTo(1220_000_000_000L);
  }
}
