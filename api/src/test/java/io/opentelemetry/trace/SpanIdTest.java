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

import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanId}. */
class SpanIdTest {
  private static final byte[] firstBytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondBytes = new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 'A'};

  @Test
  void isValid() {
    assertThat(SpanId.isValid(SpanId.getInvalid())).isFalse();
    assertThat(SpanId.isValid(SpanId.toLowerBase16(firstBytes))).isTrue();
    assertThat(SpanId.isValid(SpanId.toLowerBase16(secondBytes))).isTrue();
    assertThat(SpanId.isValid("000000000000z000")).isFalse();
  }

  @Test
  void fromLowerBase16() {
    assertThat(SpanId.toLowerBase16(SpanId.bytesFromLowerBase16("0000000000000000", 0)).toString())
        .isEqualTo(SpanId.getInvalid().toString());
    assertThat(SpanId.bytesFromLowerBase16("0000000000000061", 0)).isEqualTo(firstBytes);
    assertThat(SpanId.bytesFromLowerBase16("ff00000000000041", 0)).isEqualTo(secondBytes);
  }

  @Test
  void fromLowerBase16_WithOffset() {
    assertThat(
            SpanId.toLowerBase16(SpanId.bytesFromLowerBase16("XX0000000000000000AA", 2)).toString())
        .isEqualTo(SpanId.getInvalid().toString());
    assertThat(SpanId.bytesFromLowerBase16("YY0000000000000061BB", 2)).isEqualTo(firstBytes);
    assertThat(SpanId.bytesFromLowerBase16("ZZff00000000000041CC", 2)).isEqualTo(secondBytes);
  }

  @Test
  public void toLowerBase16() {
    assertThat(SpanId.getInvalid().toString()).isEqualTo("0000000000000000");
    assertThat(SpanId.toLowerBase16(firstBytes).toString()).isEqualTo("0000000000000061");
    assertThat(SpanId.toLowerBase16(secondBytes).toString()).isEqualTo("ff00000000000041");
  }

  @Test
  void spanId_ToString() {
    assertThat(SpanId.getInvalid().toString()).contains("0000000000000000");
    assertThat(SpanId.toLowerBase16(firstBytes).toString()).contains("0000000000000061");
    assertThat(SpanId.toLowerBase16(secondBytes).toString()).contains("ff00000000000041");
  }
}
