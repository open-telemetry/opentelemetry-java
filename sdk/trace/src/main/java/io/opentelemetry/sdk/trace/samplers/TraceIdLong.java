/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.samplers;

import java.util.Arrays;
import javax.annotation.concurrent.Immutable;

@Immutable
final class TraceIdLong {
  private static final String ALPHABET = "0123456789abcdef";
  private static final int ASCII_CHARACTERS = 128;
  private static final byte[] DECODING = buildDecodingArray();

  private static byte[] buildDecodingArray() {
    byte[] decoding = new byte[ASCII_CHARACTERS];
    Arrays.fill(decoding, (byte) -1);
    for (int i = 0; i < ALPHABET.length(); i++) {
      char c = ALPHABET.charAt(i);
      decoding[c] = (byte) i;
    }
    return decoding;
  }

  /**
   * Returns the {@code long} value whose base16 representation is stored in the first 16 chars of
   * {@code chars} starting from the {@code offset}.
   *
   * @param chars the base16 representation of the {@code long}.
   * @param offset the starting offset in the {@code CharSequence}.
   */
  static long longFromBase16String(CharSequence chars, int offset) {
    return (byteFromBase16(chars.charAt(offset), chars.charAt(offset + 1)) & 0xFFL) << 56
        | (byteFromBase16(chars.charAt(offset + 2), chars.charAt(offset + 3)) & 0xFFL) << 48
        | (byteFromBase16(chars.charAt(offset + 4), chars.charAt(offset + 5)) & 0xFFL) << 40
        | (byteFromBase16(chars.charAt(offset + 6), chars.charAt(offset + 7)) & 0xFFL) << 32
        | (byteFromBase16(chars.charAt(offset + 8), chars.charAt(offset + 9)) & 0xFFL) << 24
        | (byteFromBase16(chars.charAt(offset + 10), chars.charAt(offset + 11)) & 0xFFL) << 16
        | (byteFromBase16(chars.charAt(offset + 12), chars.charAt(offset + 13)) & 0xFFL) << 8
        | (byteFromBase16(chars.charAt(offset + 14), chars.charAt(offset + 15)) & 0xFFL);
  }

  static byte byteFromBase16(char first, char second) {
    int decoded = DECODING[first & 0x7F] << 4 | DECODING[second & 0x7F];
    return (byte) decoded;
  }

  private TraceIdLong() {}
}
