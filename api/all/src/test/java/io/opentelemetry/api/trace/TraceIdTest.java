/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.internal.BigendianEncoding;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TraceId}. */
class TraceIdTest {
  private static final String first = "00000000000000000000000000000061";
  private static final String second = "ff000000000000000000000000000041";

  @Test
  void invalid() {
    assertThat(TraceId.getInvalid()).isEqualTo("00000000000000000000000000000000");
  }

  @Test
  void isValid() {
    assertThat(TraceId.isValid(null)).isFalse();
    assertThat(TraceId.isValid("001")).isFalse();
    assertThat(TraceId.isValid("000000000000004z0000000000000016")).isFalse();
    assertThat(TraceId.isValid(TraceId.getInvalid())).isFalse();

    assertThat(TraceId.isValid(first)).isTrue();
    assertThat(TraceId.isValid(second)).isTrue();
  }

  @Test
  void fromLongs() {
    assertThat(TraceId.fromLongs(0, 0)).isEqualTo(TraceId.getInvalid());
    assertThat(TraceId.fromLongs(0, 0x61)).isEqualTo(first);
    assertThat(TraceId.fromLongs(0xff00000000000000L, 0x41)).isEqualTo(second);
    assertThat(TraceId.fromLongs(0xff01020304050600L, 0xff0a0b0c0d0e0f00L))
        .isEqualTo("ff01020304050600ff0a0b0c0d0e0f00");
  }

  @Test
  void fromBytes() {
    assertThat(TraceId.fromBytes(null)).isEqualTo(TraceId.getInvalid());

    String traceId = "0102030405060708090a0b0c0d0e0f00";
    assertThat(TraceId.fromBytes(BigendianEncoding.bytesFromBase16(traceId, TraceId.getLength())))
        .isEqualTo(traceId);
  }
}
