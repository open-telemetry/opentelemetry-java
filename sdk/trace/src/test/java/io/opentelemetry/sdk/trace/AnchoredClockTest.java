/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link AnchoredClock}. */
class AnchoredClockTest {
  private static final long EPOCH_NANOS = 1234_000_005_678L;
  private final TestClock testClock = TestClock.create(Instant.ofEpochSecond(0, EPOCH_NANOS));

  @Test
  void now_PositiveIncrease() {
    AnchoredClock anchoredClock = AnchoredClock.create(testClock);
    assertThat(anchoredClock.now()).isEqualTo(testClock.now());
    testClock.advance(Duration.ofNanos(3210));
    assertThat(anchoredClock.now()).isEqualTo(1234_000_008_888L);
    // Initial + 1000
    testClock.advance(Duration.ofNanos(-2210));
    assertThat(anchoredClock.now()).isEqualTo(1234_000_006_678L);
    testClock.advance(Duration.ofNanos(15_999_993_322L));
    assertThat(anchoredClock.now()).isEqualTo(1250_000_000_000L);
  }

  @Test
  void now_NegativeIncrease() {
    AnchoredClock anchoredClock = AnchoredClock.create(testClock);
    assertThat(anchoredClock.now()).isEqualTo(testClock.now());
    testClock.advance(Duration.ofNanos(-3456));
    assertThat(anchoredClock.now()).isEqualTo(1234_000_002_222L);
    // Initial - 1000
    testClock.advance(Duration.ofNanos(2456));
    assertThat(anchoredClock.now()).isEqualTo(1234_000_004_678L);
    testClock.advance(Duration.ofNanos(-14_000_004_678L));
    assertThat(anchoredClock.now()).isEqualTo(1220_000_000_000L);
  }
}
