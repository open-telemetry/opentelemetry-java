/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.trace;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceOptions}. */
@RunWith(JUnit4.class)
public class TraceOptionsTest {
  private static final byte FIRST_BYTE = (byte) 0xff;
  private static final byte SECOND_BYTE = 1;
  private static final byte THIRD_BYTE = 6;

  @Test
  public void getByte() {
    assertThat(TraceOptions.DEFAULT.getByte()).isEqualTo(0);
    assertThat(TraceOptions.builder().setIsSampled(false).build().getByte()).isEqualTo(0);
    assertThat(TraceOptions.builder().setIsSampled(true).build().getByte()).isEqualTo(1);
    assertThat(TraceOptions.builder().setIsSampled(true).setIsSampled(false).build().getByte())
        .isEqualTo(0);
    assertThat(TraceOptions.fromByte(FIRST_BYTE).getByte()).isEqualTo(-1);
    assertThat(TraceOptions.fromByte(SECOND_BYTE).getByte()).isEqualTo(1);
    assertThat(TraceOptions.fromByte(THIRD_BYTE).getByte()).isEqualTo(6);
  }

  @Test
  public void isSampled() {
    assertThat(TraceOptions.DEFAULT.isSampled()).isFalse();
    assertThat(TraceOptions.builder().setIsSampled(true).build().isSampled()).isTrue();
  }

  @Test
  public void toFromByte() {
    assertThat(TraceOptions.fromByte(FIRST_BYTE).getByte()).isEqualTo(FIRST_BYTE);
    assertThat(TraceOptions.fromByte(SECOND_BYTE).getByte()).isEqualTo(SECOND_BYTE);
    assertThat(TraceOptions.fromByte(THIRD_BYTE).getByte()).isEqualTo(THIRD_BYTE);
  }

  @Test
  public void toFromBase16() {
    assertThat(TraceOptions.fromLowerBase16("ff", 0).toLowerBase16()).isEqualTo("ff");
    assertThat(TraceOptions.fromLowerBase16("01", 0).toLowerBase16()).isEqualTo("01");
    assertThat(TraceOptions.fromLowerBase16("06", 0).toLowerBase16()).isEqualTo("06");
  }

  @Test
  public void builder_FromOptions() {
    assertThat(
            TraceOptions.builder(TraceOptions.fromByte(THIRD_BYTE))
                .setIsSampled(true)
                .build()
                .getByte())
        .isEqualTo(6 | 1);
  }

  @Test
  public void traceOptions_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(TraceOptions.DEFAULT);
    tester.addEqualityGroup(
        TraceOptions.fromByte(SECOND_BYTE), TraceOptions.builder().setIsSampled(true).build());
    tester.addEqualityGroup(TraceOptions.fromByte(FIRST_BYTE));
    tester.testEquals();
  }

  @Test
  public void traceOptions_ToString() {
    assertThat(TraceOptions.DEFAULT.toString()).contains("sampled=false");
    assertThat(TraceOptions.builder().setIsSampled(true).build().toString())
        .contains("sampled=true");
  }
}
