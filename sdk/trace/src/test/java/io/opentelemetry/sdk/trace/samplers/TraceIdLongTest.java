/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.CharBuffer;
import org.junit.jupiter.api.Test;

public class TraceIdLongTest {
  private static final long FIRST_LONG = 0x1213141516171819L;
  private static final char[] FIRST_CHAR_ARRAY =
      new char[] {'1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9'};
  private static final long SECOND_LONG = 0xFFEEDDCCBBAA9988L;
  private static final char[] SECOND_CHAR_ARRAY =
      new char[] {'f', 'f', 'e', 'e', 'd', 'd', 'c', 'c', 'b', 'b', 'a', 'a', '9', '9', '8', '8'};
  private static final char[] BOTH_CHAR_ARRAY =
      new char[] {
        '1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9', 'f', 'f',
        'e', 'e', 'd', 'd', 'c', 'c', 'b', 'b', 'a', 'a', '9', '9', '8', '8'
      };

  @Test
  void longFromBase16String_InputTooSmall() {
    // Valid base16 strings always have an even length.
    assertThatThrownBy(() -> TraceIdLong.longFromBase16String("12345678", 1))
        .isInstanceOf(StringIndexOutOfBoundsException.class);
  }

  @Test
  void longFromBase16String() {
    assertThat(TraceIdLong.longFromBase16String(CharBuffer.wrap(FIRST_CHAR_ARRAY), 0))
        .isEqualTo(FIRST_LONG);

    assertThat(TraceIdLong.longFromBase16String(CharBuffer.wrap(SECOND_CHAR_ARRAY), 0))
        .isEqualTo(SECOND_LONG);

    assertThat(TraceIdLong.longFromBase16String(CharBuffer.wrap(BOTH_CHAR_ARRAY), 0))
        .isEqualTo(FIRST_LONG);

    assertThat(TraceIdLong.longFromBase16String(CharBuffer.wrap(BOTH_CHAR_ARRAY), 16))
        .isEqualTo(SECOND_LONG);
  }
}
