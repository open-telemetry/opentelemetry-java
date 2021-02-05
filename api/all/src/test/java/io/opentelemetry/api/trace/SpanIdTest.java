/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanId}. */
class SpanIdTest {
  private static final String first = "0000000000000061";
  private static final byte[] firstBytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final String second = "ff00000000000041";
  private static final byte[] secondBytes = new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 'A'};

  @Test
  void invalid() {
    assertThat(SpanId.getInvalid()).isEqualTo("0000000000000000");
    assertThat(SpanId.asBytes(SpanId.getInvalid())).isEqualTo(new byte[] {0, 0, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void isValid() {
    assertThat(SpanId.isValid(SpanId.getInvalid())).isFalse();
    assertThat(SpanId.isValid(first)).isTrue();
    assertThat(SpanId.isValid(second)).isTrue();
    assertThat(SpanId.isValid("000000000000z000")).isFalse();
  }

  @Test
  void fromLowerHex() {
    assertThat(SpanId.asBytes(SpanId.getInvalid())).isEqualTo(new byte[] {0, 0, 0, 0, 0, 0, 0, 0});
    assertThat(SpanId.asBytes(first)).isEqualTo(firstBytes);
    assertThat(SpanId.asBytes(second)).isEqualTo(secondBytes);
  }

  @Test
  void fromLong() {
    assertThat(SpanId.fromLong(0)).isEqualTo(SpanId.getInvalid());
    assertThat(SpanId.fromLong(0x61)).isEqualTo(first);
    assertThat(SpanId.fromLong(0xff00000000000041L)).isEqualTo(second);
  }
}
