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
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link TestClock}. */
@RunWith(JUnit4.class)
public final class TestClockTest {

  @Test
  public void setAndGetTime() {
    TestClock clock = TestClock.create(Timestamp.create(1, 2));
    assertThat(clock.now()).isEqualTo(Timestamp.create(1, 2));
    clock.setTime(Timestamp.create(3, 4));
    assertThat(clock.now()).isEqualTo(Timestamp.create(3, 4));
  }

  @Test
  public void advanceMillis() {
    TestClock clock =
        TestClock.create(Timestamp.create(1, (int) TimeUnit.MILLISECONDS.toNanos(500)));
    clock.advanceMillis(2600);
    assertThat(clock.now())
        .isEqualTo(Timestamp.create(4, (int) TimeUnit.MILLISECONDS.toNanos(100)));
  }

  @Test
  public void measureElapsedTime() {
    TestClock clock = TestClock.create(Timestamp.create(10, 1));
    long nanos1 = clock.nowNanos();
    clock.setTime(Timestamp.create(11, 5));
    long nanos2 = clock.nowNanos();
    assertThat(nanos2 - nanos1).isEqualTo(TimeUnit.SECONDS.toNanos(1) + 4);
  }
}
