/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceId}. */
class TraceIdTest {
  private static final byte[] firstBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] secondBytes =
      new byte[] {(byte) 0xFF, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'A'};
  private static final String first = "00000000000000000000000000000061";
  private static final String second = "ff000000000000000000000000000041";

  @Test
  void invalid() {
    assertThat(TraceId.getInvalid()).isEqualTo("00000000000000000000000000000000");
    assertThat(TraceId.asBytes(TraceId.getInvalid()))
        .isEqualTo(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    assertThat(TraceId.highPartAsLong(TraceId.getInvalid())).isEqualTo(0);
    assertThat(TraceId.lowPartAsLong(TraceId.getInvalid())).isEqualTo(0);
  }

  @Test
  void isValid() {
    assertThat(TraceId.isValid(TraceId.getInvalid())).isFalse();
    assertThat(TraceId.isValid(first)).isTrue();
    assertThat(TraceId.isValid(second)).isTrue();

    assertThat(TraceId.isValid("000000000000004z0000000000000016")).isFalse();
    assertThat(TraceId.isValid("001")).isFalse();
  }

  @Test
  void testGetRandomTracePart() {
    String traceId = "0102030405060708090a0b0c0d0e0f00";
    assertThat(TraceId.getTraceIdRandomPart(traceId)).isEqualTo(0x090A0B0C0D0E0F00L);
  }

  @Test
  void testGetRandomTracePart_NegativeLongRepresentation() {
    String traceId = "ff01020304050600ff0a0b0c0d0e0f00";
    assertThat(TraceId.highPartAsLong(traceId)).isEqualTo(0xFF01020304050600L);
    assertThat(TraceId.lowPartAsLong(traceId)).isEqualTo(0xFF0A0B0C0D0E0F00L);
  }

  @Test
  void asBytes() {
    assertThat(TraceId.asBytes(TraceId.getInvalid()))
        .isEqualTo(new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
    assertThat(TraceId.asBytes("00000000000000000000000000000061")).isEqualTo(firstBytes);
    assertThat(TraceId.asBytes("ff000000000000000000000000000041")).isEqualTo(secondBytes);
  }

  @Test
  void toFromLongs() {
    Random random = new Random();
    for (int i = 0; i < 10000; i++) {
      long idHi = random.nextLong();
      long idLo = random.nextLong();
      String traceId = TraceId.fromLongs(idHi, idLo);
      assertThat(TraceId.highPartAsLong(traceId)).isEqualTo(idHi);
      assertThat(TraceId.lowPartAsLong(traceId)).isEqualTo(idLo);
    }
  }
}
