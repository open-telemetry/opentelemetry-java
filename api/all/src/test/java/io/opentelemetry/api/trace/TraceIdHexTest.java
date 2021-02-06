/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceIdHex}. */
class TraceIdHexTest {
  private static final byte[] firstBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondBytes =
      new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'A'};
  private static final String first = "00000000000000000000000000000061";
  private static final String second = "ff000000000000000000000000000041";

  @Test
  void invalid() {
    assertThat(TraceIdHex.getInvalid()).isEqualTo("00000000000000000000000000000000");
    assertThat(TraceIdHex.asBytes(TraceIdHex.getInvalid()))
        .isEqualTo(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
  }

  @Test
  void isValid() {
    assertThat(TraceIdHex.isValid(TraceIdHex.getInvalid())).isFalse();
    assertThat(TraceIdHex.isValid(first)).isTrue();
    assertThat(TraceIdHex.isValid(second)).isTrue();

    assertThat(TraceIdHex.isValid("000000000000004z0000000000000016")).isFalse();
    assertThat(TraceIdHex.isValid("001")).isFalse();
  }

  @Test
  void testGetRandomTracePart() {
    String traceId = "0102030405060708090a0b0c0d0e0f00";
    assertThat(TraceIdHex.getTraceIdRandomPart(traceId)).isEqualTo(0x090A0B0C0D0E0F00L);
  }

  @Test
  void testGetRandomTracePart_NegativeLongRepresentation() {
    String traceId = "ff01020304050600ff0a0b0c0d0e0f00";
    assertThat(TraceIdHex.getTraceIdRandomPart(traceId)).isEqualTo(0xFF0A0B0C0D0E0F00L);
  }

  @Test
  void asBytes() {
    assertThat(TraceIdHex.asBytes(TraceIdHex.getInvalid()))
        .isEqualTo(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    assertThat(TraceIdHex.asBytes(first)).isEqualTo(firstBytes);
    assertThat(TraceIdHex.asBytes(second)).isEqualTo(secondBytes);
  }

  @Test
  void fromLongs() {
    assertThat(TraceIdHex.fromLongs(0, 0)).isEqualTo(TraceIdHex.getInvalid());
    assertThat(TraceIdHex.fromLongs(0, 0x61)).isEqualTo(first);
    assertThat(TraceIdHex.fromLongs(0xff00000000000000L, 0x41)).isEqualTo(second);
    assertThat(TraceIdHex.fromLongs(0xff01020304050600L, 0xff0a0b0c0d0e0f00L))
        .isEqualTo("ff01020304050600ff0a0b0c0d0e0f00");
  }
}
