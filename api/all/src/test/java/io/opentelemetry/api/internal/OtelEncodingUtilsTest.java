/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

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
    char[] chars1 = new char[OtelEncodingUtils.LONG_BASE16];
    OtelEncodingUtils.longToBase16String(FIRST_LONG, chars1, 0);
    assertThat(chars1).isEqualTo(FIRST_CHAR_ARRAY);

    char[] chars2 = new char[OtelEncodingUtils.LONG_BASE16];
    OtelEncodingUtils.longToBase16String(SECOND_LONG, chars2, 0);
    assertThat(chars2).isEqualTo(SECOND_CHAR_ARRAY);

    char[] chars3 = new char[2 * OtelEncodingUtils.LONG_BASE16];
    OtelEncodingUtils.longToBase16String(FIRST_LONG, chars3, 0);
    OtelEncodingUtils.longToBase16String(SECOND_LONG, chars3, OtelEncodingUtils.LONG_BASE16);
    assertThat(chars3).isEqualTo(BOTH_CHAR_ARRAY);
  }

  @Test
  void longFromBase16String_InputTooSmall() {
    // Valid base16 strings always have an even length.
    assertThatThrownBy(() -> OtelEncodingUtils.longFromBase16String("12345678", 1))
        .isInstanceOf(StringIndexOutOfBoundsException.class);
  }

  @Test
  void longFromBase16String_UnrecognizedCharacters() {
    // These contain bytes not in the decoding.
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

    assertThat(
            OtelEncodingUtils.longFromBase16String(
                CharBuffer.wrap(BOTH_CHAR_ARRAY), OtelEncodingUtils.LONG_BASE16))
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
  @SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
  void invalidBytes() {
    assertThatThrownBy(() -> OtelEncodingUtils.byteFromBase16('g', 'f'))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character g");
    assertThatThrownBy(() -> OtelEncodingUtils.byteFromBase16('\u0129', 'f'))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character \u0129");
    assertThatThrownBy(() -> OtelEncodingUtils.byteFromBase16('f', 'g'))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character g");
    assertThatThrownBy(() -> OtelEncodingUtils.byteFromBase16('f', '\u0129'))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character \u0129");
  }

  private static void toFromBase16StringValidate(long value) {
    char[] dest = new char[OtelEncodingUtils.LONG_BASE16];
    OtelEncodingUtils.longToBase16String(value, dest, 0);
    assertThat(OtelEncodingUtils.longFromBase16String(CharBuffer.wrap(dest), 0)).isEqualTo(value);
  }
}
