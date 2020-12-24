/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static java.nio.CharBuffer.wrap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.CharBuffer;
import org.junit.jupiter.api.Test;

class BigendianEncodingTest {

  private static final long FIRST_LONG = 0x1213141516171819L;
  private static final byte[] FIRST_BYTE_ARRAY =
      new byte[] {0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19};
  private static final char[] FIRST_CHAR_ARRAY =
      new char[] {'1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9'};
  private static final long SECOND_LONG = 0xFFEEDDCCBBAA9988L;
  private static final byte[] SECOND_BYTE_ARRAY =
      new byte[] {
        (byte) 0xFF, (byte) 0xEE, (byte) 0xDD, (byte) 0xCC,
        (byte) 0xBB, (byte) 0xAA, (byte) 0x99, (byte) 0x88
      };
  private static final char[] SECOND_CHAR_ARRAY =
      new char[] {'f', 'f', 'e', 'e', 'd', 'd', 'c', 'c', 'b', 'b', 'a', 'a', '9', '9', '8', '8'};
  private static final byte[] BOTH_BYTE_ARRAY =
      new byte[] {
        0x12,
        0x13,
        0x14,
        0x15,
        0x16,
        0x17,
        0x18,
        0x19,
        (byte) 0xFF,
        (byte) 0xEE,
        (byte) 0xDD,
        (byte) 0xCC,
        (byte) 0xBB,
        (byte) 0xAA,
        (byte) 0x99,
        (byte) 0x88
      };
  private static final char[] BOTH_CHAR_ARRAY =
      new char[] {
        '1', '2', '1', '3', '1', '4', '1', '5', '1', '6', '1', '7', '1', '8', '1', '9', 'f', 'f',
        'e', 'e', 'd', 'd', 'c', 'c', 'b', 'b', 'a', 'a', '9', '9', '8', '8'
      };

  @Test
  void longToByteArray_Fails() {
    // These contain bytes not in the decoding.
    assertThatThrownBy(
            () -> BigendianEncoding.longToByteArray(123, new byte[BigendianEncoding.LONG_BYTES], 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("array too small");
  }

  @Test
  void longToByteArray() {
    byte[] result1 = new byte[BigendianEncoding.LONG_BYTES];
    BigendianEncoding.longToByteArray(FIRST_LONG, result1, 0);
    assertThat(result1).isEqualTo(FIRST_BYTE_ARRAY);

    byte[] result2 = new byte[BigendianEncoding.LONG_BYTES];
    BigendianEncoding.longToByteArray(SECOND_LONG, result2, 0);
    assertThat(result2).isEqualTo(SECOND_BYTE_ARRAY);

    byte[] result3 = new byte[2 * BigendianEncoding.LONG_BYTES];
    BigendianEncoding.longToByteArray(FIRST_LONG, result3, 0);
    BigendianEncoding.longToByteArray(SECOND_LONG, result3, BigendianEncoding.LONG_BYTES);
    assertThat(result3).isEqualTo(BOTH_BYTE_ARRAY);
  }

  @Test
  void longFromByteArray_ArrayToSmall() {
    // These contain bytes not in the decoding.
    assertThatThrownBy(
            () -> BigendianEncoding.longFromByteArray(new byte[BigendianEncoding.LONG_BYTES], 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("array too small");
  }

  @Test
  void longFromByteArray() {
    assertThat(BigendianEncoding.longFromByteArray(FIRST_BYTE_ARRAY, 0)).isEqualTo(FIRST_LONG);

    assertThat(BigendianEncoding.longFromByteArray(SECOND_BYTE_ARRAY, 0)).isEqualTo(SECOND_LONG);

    assertThat(BigendianEncoding.longFromByteArray(BOTH_BYTE_ARRAY, 0)).isEqualTo(FIRST_LONG);

    assertThat(BigendianEncoding.longFromByteArray(BOTH_BYTE_ARRAY, BigendianEncoding.LONG_BYTES))
        .isEqualTo(SECOND_LONG);
  }

  @Test
  void toFromByteArray() {
    toFromByteArrayValidate(0x8000000000000000L);
    toFromByteArrayValidate(-1);
    toFromByteArrayValidate(0);
    toFromByteArrayValidate(1);
    toFromByteArrayValidate(0x7FFFFFFFFFFFFFFFL);
  }

  @Test
  void longToBase16String() {
    char[] chars1 = new char[BigendianEncoding.LONG_BASE16];
    BigendianEncoding.longToBase16String(FIRST_LONG, chars1, 0);
    assertThat(chars1).isEqualTo(FIRST_CHAR_ARRAY);

    char[] chars2 = new char[BigendianEncoding.LONG_BASE16];
    BigendianEncoding.longToBase16String(SECOND_LONG, chars2, 0);
    assertThat(chars2).isEqualTo(SECOND_CHAR_ARRAY);

    char[] chars3 = new char[2 * BigendianEncoding.LONG_BASE16];
    BigendianEncoding.longToBase16String(FIRST_LONG, chars3, 0);
    BigendianEncoding.longToBase16String(SECOND_LONG, chars3, BigendianEncoding.LONG_BASE16);
    assertThat(chars3).isEqualTo(BOTH_CHAR_ARRAY);
  }

  @Test
  void longFromBase16String_InputTooSmall() {
    // Valid base16 strings always have an even length.
    assertThatThrownBy(
            () ->
                BigendianEncoding.longFromBase16String(
                    wrap(new char[BigendianEncoding.LONG_BASE16]), 1))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("chars too small");
  }

  @Test
  void longFromBase16String_UnrecognizedCharacters() {
    // These contain bytes not in the decoding.
    assertThatThrownBy(() -> BigendianEncoding.longFromBase16String("0123456789gbcdef", 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character g");
  }

  @Test
  void validHex() {
    assertThat(BigendianEncoding.isValidBase16String("abcdef1234567890")).isTrue();
    assertThat(BigendianEncoding.isValidBase16String("abcdefg1234567890")).isFalse();
    assertThat(BigendianEncoding.isValidBase16String("<abcdef1234567890")).isFalse();
    assertThat(BigendianEncoding.isValidBase16String("(abcdef1234567890")).isFalse();
    assertThat(BigendianEncoding.isValidBase16String("abcdef1234567890B")).isFalse();
  }

  @Test
  void longFromBase16String() {
    assertThat(BigendianEncoding.longFromBase16String(CharBuffer.wrap(FIRST_CHAR_ARRAY), 0))
        .isEqualTo(FIRST_LONG);

    assertThat(BigendianEncoding.longFromBase16String(CharBuffer.wrap(SECOND_CHAR_ARRAY), 0))
        .isEqualTo(SECOND_LONG);

    assertThat(BigendianEncoding.longFromBase16String(CharBuffer.wrap(BOTH_CHAR_ARRAY), 0))
        .isEqualTo(FIRST_LONG);

    assertThat(
            BigendianEncoding.longFromBase16String(
                CharBuffer.wrap(BOTH_CHAR_ARRAY), BigendianEncoding.LONG_BASE16))
        .isEqualTo(SECOND_LONG);
  }

  @Test
  void toFromBase16String() {
    toFromBase16StringValidate(0x8000000000000000L);
    toFromBase16StringValidate(-1);
    toFromBase16StringValidate(0);
    toFromBase16StringValidate(1);
    toFromBase16StringValidate(0x7FFFFFFFFFFFFFFFL);
  }

  @Test
  void invalidOffset() {
    assertThatThrownBy(() -> BigendianEncoding.byteFromBase16String("fff", 2))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("chars too small");
  }

  @Test
  @SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
  void invalidBytes() {
    assertThatThrownBy(() -> BigendianEncoding.byteFromBase16String("gf", 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character g");
    assertThatThrownBy(() -> BigendianEncoding.byteFromBase16String("\u0129f", 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character \u0129");
    assertThatThrownBy(() -> BigendianEncoding.byteFromBase16String("fg", 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character g");
    assertThatThrownBy(() -> BigendianEncoding.byteFromBase16String("f\u0129", 0))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character \u0129");
  }

  private static void toFromByteArrayValidate(long value) {
    byte[] array = new byte[BigendianEncoding.LONG_BYTES];
    BigendianEncoding.longToByteArray(value, array, 0);
    assertThat(BigendianEncoding.longFromByteArray(array, 0)).isEqualTo(value);
  }

  private static void toFromBase16StringValidate(long value) {
    char[] dest = new char[BigendianEncoding.LONG_BASE16];
    BigendianEncoding.longToBase16String(value, dest, 0);
    assertThat(BigendianEncoding.longFromBase16String(CharBuffer.wrap(dest), 0)).isEqualTo(value);
  }
}
