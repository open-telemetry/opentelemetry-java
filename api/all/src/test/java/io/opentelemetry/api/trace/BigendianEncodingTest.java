/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BigendianEncodingTest {

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
  void validHex() {
    assertThat(BigendianEncoding.isValidBase16String("abcdef1234567890")).isTrue();
    assertThat(BigendianEncoding.isValidBase16String("abcdefg1234567890")).isFalse();
    assertThat(BigendianEncoding.isValidBase16String("<abcdef1234567890")).isFalse();
    assertThat(BigendianEncoding.isValidBase16String("(abcdef1234567890")).isFalse();
    assertThat(BigendianEncoding.isValidBase16String("abcdef1234567890B")).isFalse();
  }

  @Test
  @SuppressWarnings("checkstyle:AvoidEscapedUnicodeCharacters")
  void invalidBytes() {
    assertThatThrownBy(() -> BigendianEncoding.byteFromBase16('g', 'f'))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character g");
    assertThatThrownBy(() -> BigendianEncoding.byteFromBase16('\u0129', 'f'))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character \u0129");
    assertThatThrownBy(() -> BigendianEncoding.byteFromBase16('f', 'g'))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character g");
    assertThatThrownBy(() -> BigendianEncoding.byteFromBase16('f', '\u0129'))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("invalid character \u0129");
  }
}
