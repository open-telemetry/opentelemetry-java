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

package io.opentelemetry.sdk.common;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link Timestamp}. */
@RunWith(JUnit4.class)
public class TimestampTest {
  @Rule public ExpectedException thrown = ExpectedException.none();

  @Test
  public void timestampCreate() {
    assertThat(Timestamp.create(24, 42).getSeconds()).isEqualTo(24);
    assertThat(Timestamp.create(24, 42).getNanos()).isEqualTo(42);
    assertThat(Timestamp.create(-24, 42).getSeconds()).isEqualTo(-24);
    assertThat(Timestamp.create(-24, 42).getNanos()).isEqualTo(42);
    assertThat(Timestamp.create(315576000000L, 999999999).getSeconds()).isEqualTo(315576000000L);
    assertThat(Timestamp.create(315576000000L, 999999999).getNanos()).isEqualTo(999999999);
    assertThat(Timestamp.create(-315576000000L, 999999999).getSeconds()).isEqualTo(-315576000000L);
    assertThat(Timestamp.create(-315576000000L, 999999999).getNanos()).isEqualTo(999999999);
  }

  @Test
  public void create_SecondsTooLow() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'seconds' is less than minimum (-315576000000): -315576000001");
    Timestamp.create(-315576000001L, 0);
  }

  @Test
  public void create_SecondsTooHigh() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'seconds' is greater than maximum (315576000000): 315576000001");
    Timestamp.create(315576000001L, 0);
  }

  @Test
  public void create_NanosTooLow_PositiveTime() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'nanos' is less than zero: -1");
    Timestamp.create(1, -1);
  }

  @Test
  public void create_NanosTooHigh_PositiveTime() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'nanos' is greater than maximum (999999999): 1000000000");
    Timestamp.create(1, 1000000000);
  }

  @Test
  public void create_NanosTooLow_NegativeTime() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'nanos' is less than zero: -1");
    Timestamp.create(-1, -1);
  }

  @Test
  public void create_NanosTooHigh_NegativeTime() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'nanos' is greater than maximum (999999999): 1000000000");
    Timestamp.create(-1, 1000000000);
  }

  @Test
  public void timestampFromMillis() {
    assertThat(Timestamp.fromMillis(0)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.fromMillis(987)).isEqualTo(Timestamp.create(0, 987000000));
    assertThat(Timestamp.fromMillis(3456)).isEqualTo(Timestamp.create(3, 456000000));
  }

  @Test
  public void timestampFromMillis_Negative() {
    assertThat(Timestamp.fromMillis(-1)).isEqualTo(Timestamp.create(-1, 999000000));
    assertThat(Timestamp.fromMillis(-999)).isEqualTo(Timestamp.create(-1, 1000000));
    assertThat(Timestamp.fromMillis(-3456)).isEqualTo(Timestamp.create(-4, 544000000));
  }

  @Test
  public void fromMillis_TooLow() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'seconds' is less than minimum (-315576000000): -315576000001");
    Timestamp.fromMillis(-315576000001000L);
  }

  @Test
  public void fromMillis_TooHigh() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("'seconds' is greater than maximum (315576000000): 315576000001");
    Timestamp.fromMillis(315576000001000L);
  }

  @Test
  public void timestamp_Equal() {
    // Positive tests.
    assertThat(Timestamp.create(0, 0)).isEqualTo(Timestamp.create(0, 0));
    assertThat(Timestamp.create(24, 42)).isEqualTo(Timestamp.create(24, 42));
    assertThat(Timestamp.create(-24, 42)).isEqualTo(Timestamp.create(-24, 42));
    // Negative tests.
    assertThat(Timestamp.create(25, 42)).isNotEqualTo(Timestamp.create(24, 42));
    assertThat(Timestamp.create(24, 43)).isNotEqualTo(Timestamp.create(24, 42));
    assertThat(Timestamp.create(-25, 42)).isNotEqualTo(Timestamp.create(-24, 42));
    assertThat(Timestamp.create(-24, 43)).isNotEqualTo(Timestamp.create(-24, 42));
  }
}
