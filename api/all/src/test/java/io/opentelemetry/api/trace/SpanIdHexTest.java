/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanIdHex}. */
class SpanIdHexTest {
  private static final String first = "0000000000000061";
  private static final byte[] firstBytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final String second = "ff00000000000041";
  private static final byte[] secondBytes = new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 'A'};

  @Test
  void invalid() {
    assertThat(SpanIdHex.getInvalid()).isEqualTo("0000000000000000");
    assertThat(SpanIdHex.asBytes(SpanIdHex.getInvalid()))
        .isEqualTo(new byte[] {0, 0, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void isValid() {
    assertThat(SpanIdHex.isValid(SpanIdHex.getInvalid())).isFalse();
    assertThat(SpanIdHex.isValid(first)).isTrue();
    assertThat(SpanIdHex.isValid(second)).isTrue();
    assertThat(SpanIdHex.isValid("000000000000z000")).isFalse();
  }

  @Test
  void fromLowerHex() {
    assertThat(SpanIdHex.asBytes(SpanIdHex.getInvalid()))
        .isEqualTo(new byte[] {0, 0, 0, 0, 0, 0, 0, 0});
    assertThat(SpanIdHex.asBytes(first)).isEqualTo(firstBytes);
    assertThat(SpanIdHex.asBytes(second)).isEqualTo(secondBytes);
  }

  @Test
  void fromLong() {
    assertThat(SpanIdHex.fromLong(0)).isEqualTo(SpanIdHex.getInvalid());
    assertThat(SpanIdHex.fromLong(0x61)).isEqualTo(first);
    assertThat(SpanIdHex.fromLong(0xff00000000000041L)).isEqualTo(second);
  }
}
