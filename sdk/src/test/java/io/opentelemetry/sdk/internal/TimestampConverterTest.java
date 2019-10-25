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

import io.opentelemetry.sdk.common.Timestamp;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TimestampConverter}. */
@RunWith(JUnit4.class)
public class TimestampConverterTest {
  private final Timestamp timestamp = Timestamp.create(1234, 5678);
  private final TestClock testClock = TestClock.create(timestamp);

  @Test
  public void now() {
    assertThat(testClock.now()).isEqualTo(timestamp);
    TimestampConverter timeConverter = TimestampConverter.now(testClock);
    assertThat(timeConverter.convertNanoTime(testClock.nowNanos())).isEqualTo(timestamp);
  }

  @Test
  public void convertNanoTime_Positive() {
    TimestampConverter timeConverter = TimestampConverter.now(testClock);
    assertThat(timeConverter.convertNanoTime(testClock.nowNanos() + 3210))
        .isEqualTo(Timestamp.create(1234, 8888));
    assertThat(timeConverter.convertNanoTime(testClock.nowNanos() + 1000))
        .isEqualTo(Timestamp.create(1234, 6678));
    assertThat(timeConverter.convertNanoTime(testClock.nowNanos() + 15_999_994_322L))
        .isEqualTo(Timestamp.create(1250, 0));
  }

  @Test
  public void convertNanoTime_Negative() {
    TimestampConverter timeConverter = TimestampConverter.now(testClock);
    assertThat(timeConverter.convertNanoTime(testClock.nowNanos() - 3456))
        .isEqualTo(Timestamp.create(1234, 2222));
    assertThat(timeConverter.convertNanoTime(testClock.nowNanos() - 1000))
        .isEqualTo(Timestamp.create(1234, 4678));
    assertThat(timeConverter.convertNanoTime(testClock.nowNanos() - 14000005678L))
        .isEqualTo(Timestamp.create(1220, 0));
  }
}
