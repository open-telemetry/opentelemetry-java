/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.CharBuffer;
import org.junit.jupiter.api.Test;

class OtelEncodingUtilsTest {

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
  void longToBase16String() {
    char[] chars1 = new char[16];
    OtelEncodingUtils.longToBase16String(FIRST_LONG, chars1, 0);
    assertThat(chars1).isEqualTo(FIRST_CHAR_ARRAY);

    char[] chars2 = new char[16];
    OtelEncodingUtils.longToBase16String(SECOND_LONG, chars2, 0);
    assertThat(chars2).isEqualTo(SECOND_CHAR_ARRAY);

    char[] chars3 = new char[32];
    OtelEncodingUtils.longToBase16String(FIRST_LONG, chars3, 0);
    OtelEncodingUtils.longToBase16String(SECOND_LONG, chars3, 16);
    assertThat(chars3).isEqualTo(BOTH_CHAR_ARRAY);
  }

  @Test
  void longFromBase16String_InputTooSmall() {
    assertThatThrownBy(() -> OtelEncodingUtils.longFromBase16String("12345678", 1))
        .isInstanceOf(StringIndexOutOfBoundsException.class);
  }

  @Test
  void longFromBase16String_UnrecognizedCharacters() {
    assertThatThrownBy(() -> OtelEncodingUtils.longFromBase16String("0123456789gbcdef", 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character g");
  }

  @Test
  void validHex() {
    assertThat(OtelEncodingUtils.isValidBase16String("abcdef1234567890")).isTrue();
    assertThat(OtelEncodingUtils.isValidBase16String("abcdefg1234567890")).isFalse();
    assertThat(OtelEncodingUtils.isValidBase16String("<abcdef1234567890")).isFalse();
    assertThat(OtelEncodingUtils.isValidBase16String("(abcdef1234567890")).isFalse();
    assertThat(OtelEncodingUtils.isValidBase16String("abcdef1234567890B")).isFalse();
  }

  @Test
  void longFromBase16String() {
    assertThat(OtelEncodingUtils.longFromBase16String(CharBuffer.wrap(FIRST_CHAR_ARRAY), 0))
        .isEqualTo(FIRST_LONG);

    assertThat(OtelEncodingUtils.longFromBase16String(CharBuffer.wrap(SECOND_CHAR_ARRAY), 0))
        .isEqualTo(SECOND_LONG);

    assertThat(OtelEncodingUtils.longFromBase16String(CharBuffer.wrap(BOTH_CHAR_ARRAY), 0))
        .isEqualTo(FIRST_LONG);

    assertThat(OtelEncodingUtils.longFromBase16String(CharBuffer.wrap(BOTH_CHAR_ARRAY), 16))
        .isEqualTo(SECOND_LONG);
  }

  @Test
  void bytesFromBase16() {
    byte[] bytes = OtelEncodingUtils.bytesFromBase16("0102030405060708090a0b0c0d0e0f10", 32);
    assertThat(bytes).containsExactly(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);

    byte[] dest = new byte[16];
    OtelEncodingUtils.bytesFromBase16("0102030405060708090a0b0c0d0e0f10", 32, dest);
    assertThat(dest).isEqualTo(bytes);
  }

  @Test
  void toFromBase16String() {
    toFromBase16StringValidate(0x8000000000000000L);
    toFromBase16StringValidate(-1);
    toFromBase16StringValidate(0);
    toFromBase16StringValidate(1);
    toFromBase16StringValidate(0x7FFFFFFFFFFFFFFFL);
  }

  private static void toFromBase16StringValidate(long value) {
    char[] dest = new char[16];
    OtelEncodingUtils.longToBase16String(value, dest, 0);
    assertThat(OtelEncodingUtils.longFromBase16String(CharBuffer.wrap(dest), 0)).isEqualTo(value);
  }
}
