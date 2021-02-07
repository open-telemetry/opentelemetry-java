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
  void invalid() {
    assertThat(SpanId.getInvalid()).isEqualTo("0000000000000000");
    assertThat(SpanId.asBytes(SpanId.getInvalid())).isEqualTo(new byte[] {0, 0, 0, 0, 0, 0, 0, 0});
    assertThat(SpanId.asLong(SpanId.getInvalid())).isEqualTo(0);
  }

  @Test
  void isValid() {
    assertThat(SpanId.isValid(SpanId.getInvalid())).isFalse();
    assertThat(SpanId.isValid(SpanId.fromBytes(firstBytes))).isTrue();
    assertThat(SpanId.isValid(SpanId.fromBytes(secondBytes))).isTrue();
    assertThat(SpanId.isValid("000000000000z000")).isFalse();
  }

  @Test
  void fromLowerHex() {
    assertThat(SpanId.fromBytes(SpanId.asBytes("0000000000000000"))).isEqualTo(SpanId.getInvalid());
    assertThat(SpanId.asBytes("0000000000000061")).isEqualTo(firstBytes);
    assertThat(SpanId.asBytes("ff00000000000041")).isEqualTo(secondBytes);
  }

  @Test
  public void toLowerHex() {
    assertThat(SpanId.getInvalid()).isEqualTo("0000000000000000");
    assertThat(SpanId.fromBytes(firstBytes)).isEqualTo("0000000000000061");
    assertThat(SpanId.fromBytes(secondBytes)).isEqualTo("ff00000000000041");
  }

  @Test
  void toFromLong() {
    Random random = new Random();
    for (int i = 0; i < 1000; i++) {
      long id = random.nextLong();
      assertThat(SpanId.asLong(SpanId.fromLong(id))).isEqualTo(id);
    }
  }
}
