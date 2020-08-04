/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.internal;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link MonotonicClock}. */
class MonotonicClockTest {
  private static final long EPOCH_NANOS = 1234_000_005_678L;
  private final TestClock testClock = TestClock.create(EPOCH_NANOS);

  @Test
  void nanoTime() {
    assertThat(testClock.now()).isEqualTo(EPOCH_NANOS);
    MonotonicClock monotonicClock = MonotonicClock.create(testClock);
    assertThat(monotonicClock.nanoTime()).isEqualTo(testClock.nanoTime());
    testClock.advanceNanos(12345);
    assertThat(monotonicClock.nanoTime()).isEqualTo(testClock.nanoTime());
  }

  @Test
  void now_PositiveIncrease() {
    MonotonicClock monotonicClock = MonotonicClock.create(testClock);
    assertThat(monotonicClock.now()).isEqualTo(testClock.now());
    testClock.advanceNanos(3210);
    assertThat(monotonicClock.now()).isEqualTo(1234_000_008_888L);
    // Initial + 1000
    testClock.advanceNanos(-2210);
    assertThat(monotonicClock.now()).isEqualTo(1234_000_006_678L);
    testClock.advanceNanos(15_999_993_322L);
    assertThat(monotonicClock.now()).isEqualTo(1250_000_000_000L);
  }

  @Test
  void now_NegativeIncrease() {
    MonotonicClock monotonicClock = MonotonicClock.create(testClock);
    assertThat(monotonicClock.now()).isEqualTo(testClock.now());
    testClock.advanceNanos(-3456);
    assertThat(monotonicClock.now()).isEqualTo(1234_000_002_222L);
    // Initial - 1000
    testClock.advanceNanos(2456);
    assertThat(monotonicClock.now()).isEqualTo(1234_000_004_678L);
    testClock.advanceNanos(-14_000_004_678L);
    assertThat(monotonicClock.now()).isEqualTo(1220_000_000_000L);
  }
}
