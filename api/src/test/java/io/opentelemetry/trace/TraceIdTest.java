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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.EqualsTester;
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TraceId}. */
@RunWith(JUnit4.class)
public class TraceIdTest {
  private static final byte[] firstBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondBytes =
      new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'A'};
  private static final TraceId first = TraceId.fromBytes(firstBytes, 0);

  private static final TraceId second =
      new TraceId(
          ByteBuffer.wrap(secondBytes).getLong(), ByteBuffer.wrap(secondBytes, 8, 8).getLong());

  @Test
  public void invalidTraceId() {
    assertThat(TraceId.getTraceIdRandomPart(TraceId.getInvalid())).isEqualTo(0);
  }

  @Test
  public void isValid() {
    assertThat(TraceId.isValid(TraceId.getInvalid())).isFalse();
    assertThat(first.isValid()).isTrue();
    assertThat(second.isValid()).isTrue();
  }

  @Test
  public void testGetRandomTracePart() {
    byte[] id = {
      0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x00
    };
    TraceId traceid = TraceId.fromBytes(id, 0);
    assertThat(traceid.getTraceRandomPart()).isEqualTo(0x090A0B0C0D0E0F00L);
  }

  @Test
  public void testGetRandomTracePart_NegativeLongRepresentation() {
    byte[] id = {
      0x01,
      0x02,
      0x03,
      0x04,
      0x05,
      0x06,
      0x07,
      0x08,
      (byte) 0xFF, // force a negative value
      0x0A,
      0x0B,
      0x0C,
      0x0D,
      0x0E,
      0x0F,
      0x00
    };
    TraceId traceid = TraceId.fromBytes(id, 0);
    assertThat(traceid.getTraceRandomPart()).isEqualTo(0xFF0A0B0C0D0E0F00L);
  }

  @Test
  public void fromLowerBase16() {
    assertThat(TraceId.bytesFromLowerBase16("00000000000000000000000000000000", 0))
        .isEqualTo(TraceId.getInvalid());
    assertThat(TraceId.bytesFromLowerBase16("00000000000000000000000000000061", 0))
        .isEqualTo(firstBytes);
    assertThat(TraceId.bytesFromLowerBase16("ff000000000000000000000000000041", 0))
        .isEqualTo(secondBytes);
  }

  @Test
  public void fromLowerBase16_WithOffset() {
    assertThat(TraceId.bytesFromLowerBase16("XX00000000000000000000000000000000CC", 2))
        .isEqualTo(TraceId.getInvalid());
    assertThat(TraceId.bytesFromLowerBase16("YY00000000000000000000000000000061AA", 2))
        .isEqualTo(firstBytes);
    assertThat(TraceId.bytesFromLowerBase16("ZZff000000000000000000000000000041BB", 2))
        .isEqualTo(secondBytes);
  }

  @Test
  public void toLowerBase16() {
    assertThat(TraceId.toLowerBase16(TraceId.getInvalid()))
        .isEqualTo("00000000000000000000000000000000");
    assertThat(TraceId.toLowerBase16(firstBytes)).isEqualTo("00000000000000000000000000000061");
    assertThat(TraceId.toLowerBase16(secondBytes)).isEqualTo("ff000000000000000000000000000041");
  }

  @Test
  public void traceId_CompareTo() {
    assertThat(first.compareTo(second)).isGreaterThan(0);
    assertThat(second.compareTo(first)).isLessThan(0);
    assertThat(first.compareTo(TraceId.fromBytes(firstBytes, 0))).isEqualTo(0);
  }

  @Test
  public void traceId_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        first, TraceId.fromBytes(Arrays.copyOf(firstBytes, firstBytes.length), 0));
    tester.addEqualityGroup(
        second, TraceId.fromBytes(Arrays.copyOf(secondBytes, secondBytes.length), 0));
    tester.testEquals();
  }

  @Test
  public void traceId_ToString() {
    assertThat(TraceId.toLowerBase16(TraceId.getInvalid()))
        .contains("00000000000000000000000000000000");
    assertThat(TraceId.toLowerBase16(firstBytes)).contains("00000000000000000000000000000061");
    assertThat(TraceId.toLowerBase16(secondBytes)).contains("ff000000000000000000000000000041");
  }
}
