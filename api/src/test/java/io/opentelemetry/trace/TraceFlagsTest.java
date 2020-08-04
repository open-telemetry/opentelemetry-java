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

package io.opentelemetry.trace;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceFlags}. */
class TraceFlagsTest {
  private static final byte FIRST_BYTE = (byte) 0xff;
  private static final byte SECOND_BYTE = 1;
  private static final byte THIRD_BYTE = 6;

  @Test
  void getByte() {
    assertThat(TraceFlags.getDefault().getByte()).isZero();
    assertThat(TraceFlags.builder().setIsSampled(false).build().getByte()).isZero();
    assertThat(TraceFlags.builder().setIsSampled(true).build().getByte()).isOne();
    assertThat(TraceFlags.builder().setIsSampled(true).setIsSampled(false).build().getByte())
        .isZero();
    assertThat(TraceFlags.fromByte(FIRST_BYTE).getByte()).isEqualTo((byte) -1);
    assertThat(TraceFlags.fromByte(SECOND_BYTE).getByte()).isOne();
    assertThat(TraceFlags.fromByte(THIRD_BYTE).getByte()).isEqualTo((byte) 6);
  }

  @Test
  void isSampled() {
    assertThat(TraceFlags.getDefault().isSampled()).isFalse();
    assertThat(TraceFlags.builder().setIsSampled(true).build().isSampled()).isTrue();
  }

  @Test
  void toFromByte() {
    assertThat(TraceFlags.fromByte(FIRST_BYTE).getByte()).isEqualTo(FIRST_BYTE);
    assertThat(TraceFlags.fromByte(SECOND_BYTE).getByte()).isEqualTo(SECOND_BYTE);
    assertThat(TraceFlags.fromByte(THIRD_BYTE).getByte()).isEqualTo(THIRD_BYTE);
  }

  @Test
  void toFromBase16() {
    assertThat(TraceFlags.fromLowerBase16("ff", 0).toLowerBase16()).isEqualTo("ff");
    assertThat(TraceFlags.fromLowerBase16("01", 0).toLowerBase16()).isEqualTo("01");
    assertThat(TraceFlags.fromLowerBase16("06", 0).toLowerBase16()).isEqualTo("06");
  }

  @Test
  void builder_FromOptions() {
    assertThat(
            TraceFlags.builder(TraceFlags.fromByte(THIRD_BYTE))
                .setIsSampled(true)
                .build()
                .getByte())
        .isEqualTo((byte) (6 | 1));
  }

  @Test
  void traceFlags_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(TraceFlags.getDefault());
    tester.addEqualityGroup(
        TraceFlags.fromByte(SECOND_BYTE), TraceFlags.builder().setIsSampled(true).build());
    tester.addEqualityGroup(TraceFlags.fromByte(FIRST_BYTE));
    tester.testEquals();
  }

  @Test
  void traceFlags_ToString() {
    assertThat(TraceFlags.getDefault().toString()).contains("sampled=false");
    assertThat(TraceFlags.builder().setIsSampled(true).build().toString()).contains("sampled=true");
  }
}
