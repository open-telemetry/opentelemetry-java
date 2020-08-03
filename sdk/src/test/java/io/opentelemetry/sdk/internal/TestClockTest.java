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
