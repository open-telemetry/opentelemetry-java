/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanId}. */
class SpanIdTest {
  private static final byte[] firstBytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondBytes = new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 'A'};

  @Test
  void isValid() {
    assertThat(SpanId.isValid(SpanId.getInvalid())).isFalse();
    assertThat(SpanId.isValid(SpanId.bytesToHex(firstBytes))).isTrue();
    assertThat(SpanId.isValid(SpanId.bytesToHex(secondBytes))).isTrue();
    assertThat(SpanId.isValid("000000000000z000")).isFalse();
  }

  @Test
  void fromLowerBase16() {
    assertThat(SpanId.bytesToHex(SpanId.bytesFromHex("0000000000000000", 0)))
        .isEqualTo(SpanId.getInvalid());
    assertThat(SpanId.bytesFromHex("0000000000000061", 0)).isEqualTo(firstBytes);
    assertThat(SpanId.bytesFromHex("ff00000000000041", 0)).isEqualTo(secondBytes);
  }

  @Test
  void fromLowerBase16_WithOffset() {
    assertThat(SpanId.bytesToHex(SpanId.bytesFromHex("XX0000000000000000AA", 2)))
        .isEqualTo(SpanId.getInvalid());
    assertThat(SpanId.bytesFromHex("YY0000000000000061BB", 2)).isEqualTo(firstBytes);
    assertThat(SpanId.bytesFromHex("ZZff00000000000041CC", 2)).isEqualTo(secondBytes);
  }

  @Test
  public void toLowerBase16() {
    assertThat(SpanId.getInvalid()).isEqualTo("0000000000000000");
    assertThat(SpanId.bytesToHex(firstBytes)).isEqualTo("0000000000000061");
    assertThat(SpanId.bytesToHex(secondBytes)).isEqualTo("ff00000000000041");
  }

  @Test
  void spanId_ToString() {
    assertThat(SpanId.getInvalid()).contains("0000000000000000");
    assertThat(SpanId.bytesToHex(firstBytes)).contains("0000000000000061");
    assertThat(SpanId.bytesToHex(secondBytes)).contains("ff00000000000041");
  }

  @Test
  void toAndFromLong() {
    Random random = new Random();
    for (int i = 0; i < 1000; i++) {
      long id = random.nextLong();
      assertThat(SpanId.asLong(SpanId.fromLong(id))).isEqualTo(id);
    }
  }

  @Test
  void size() {
    assertThat(SpanId.getSize()).isEqualTo(8);
  }
}
