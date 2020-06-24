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
import java.nio.ByteBuffer;
import java.util.Arrays;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanId}. */
class SpanIdTest {
  private static final byte[] firstBytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondBytes = new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 'A'};
  private static final SpanId first = new SpanId(ByteBuffer.wrap(firstBytes).getLong());
  private static final SpanId second = SpanId.fromBytes(secondBytes, 0);

  @Test
  void isValid() {
    assertThat(SpanId.isValid(SpanId.getInvalid())).isFalse();
    assertThat(SpanId.isValid(firstBytes)).isTrue();
    assertThat(SpanId.isValid(secondBytes)).isTrue();
  }

  @Test
  void fromLowerBase16() {
    assertThat(SpanId.bytesFromLowerBase16("0000000000000000", 0)).isEqualTo(SpanId.getInvalid());
    assertThat(SpanId.bytesFromLowerBase16("0000000000000061", 0)).isEqualTo(firstBytes);
    assertThat(SpanId.bytesFromLowerBase16("ff00000000000041", 0)).isEqualTo(secondBytes);
  }

  @Test
  void fromLowerBase16_WithOffset() {
    assertThat(SpanId.bytesFromLowerBase16("XX0000000000000000AA", 2))
        .isEqualTo(SpanId.getInvalid());
    assertThat(SpanId.bytesFromLowerBase16("YY0000000000000061BB", 2)).isEqualTo(firstBytes);
    assertThat(SpanId.bytesFromLowerBase16("ZZff00000000000041CC", 2)).isEqualTo(secondBytes);
  }

  @Test
  void toLowerBase16() {
    assertThat(SpanId.toLowerBase16(SpanId.getInvalid())).isEqualTo("0000000000000000");
    assertThat(SpanId.toLowerBase16(firstBytes)).isEqualTo("0000000000000061");
    assertThat(SpanId.toLowerBase16(secondBytes)).isEqualTo("ff00000000000041");
  }

  @Test
  void spanId_CompareTo() {
    assertThat(first.compareTo(second)).isGreaterThan(0);
    assertThat(second.compareTo(first)).isLessThan(0);
    assertThat(first.compareTo(SpanId.fromBytes(firstBytes, 0))).isEqualTo(0);
  }

  @Test
  void spanId_EqualsAndHashCode() {
    EqualsTester tester = new EqualsTester();
    tester.addEqualityGroup(
        first, SpanId.fromBytes(Arrays.copyOf(firstBytes, firstBytes.length), 0));
    tester.addEqualityGroup(
        second, SpanId.fromBytes(Arrays.copyOf(secondBytes, secondBytes.length), 0));
    tester.testEquals();
  }

  @Test
  void spanId_ToString() {
    assertThat(SpanId.toLowerBase16(SpanId.getInvalid())).contains("0000000000000000");
    assertThat(SpanId.toLowerBase16(firstBytes)).contains("0000000000000061");
    assertThat(SpanId.toLowerBase16(secondBytes)).contains("ff00000000000041");
  }
}
