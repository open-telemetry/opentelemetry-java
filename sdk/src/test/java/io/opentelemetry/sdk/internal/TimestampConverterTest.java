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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TimestampConverter}. */
@RunWith(JUnit4.class)
public class TimestampConverterTest {
  private final long epochNanos = 1234_000_005_678L;
  private final TestClock testClock = TestClock.create(epochNanos);

  @Test
  public void now() {
    assertThat(testClock.now()).isEqualTo(epochNanos);
    TimestampConverter timeConverter = TimestampConverter.now(testClock);
    assertThat(timeConverter.convertNanoTime(testClock.nanoTime())).isEqualTo(epochNanos);
  }

  @Test
  public void convertNanoTime_Positive() {
    TimestampConverter timeConverter = TimestampConverter.now(testClock);
    assertThat(timeConverter.convertNanoTime(testClock.nanoTime() + 3210))
        .isEqualTo(1234_000_008_888L);
    assertThat(timeConverter.convertNanoTime(testClock.nanoTime() + 1000))
        .isEqualTo(1234_000_006_678L);
    assertThat(timeConverter.convertNanoTime(testClock.nanoTime() + 15_999_994_322L))
        .isEqualTo(1250_000_000_000L);
  }

  @Test
  public void convertNanoTime_Negative() {
    TimestampConverter timeConverter = TimestampConverter.now(testClock);
    assertThat(timeConverter.convertNanoTime(testClock.nanoTime() - 3456))
        .isEqualTo(1234_000_002_222L);
    assertThat(timeConverter.convertNanoTime(testClock.nanoTime() - 1000))
        .isEqualTo(1234_000_004_678L);
    assertThat(timeConverter.convertNanoTime(testClock.nanoTime() - 14000005678L))
        .isEqualTo(1220_000_000_000L);
  }
}
