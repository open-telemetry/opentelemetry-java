/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import javax.annotation.concurrent.Immutable;

/**
 * Helper methods for dealing with a span identifier. A valid span identifier is an 8-byte array
 * with at least one non-zero byte. In lowercase hex (base16) representation, a 16 character hex
 * String, where at least one of the characters is not a '0'.
 */
@Immutable
public final class SpanId {
  private static final ThreadLocal<char[]> charBuffer = new ThreadLocal<>();

  private static final int HEX_SIZE = 16;
  private static final String INVALID = "0000000000000000";

  private SpanId() {}

  /** Returns the length of the lowercase hex (base16) representation of the {@code SpanId}. */
  public static int getHexLength() {
    return HEX_SIZE;
  }

  /**
   * Returns the invalid {@code SpanId} in lowercase hex (base16) representation. All characters are
   * '\0'.
   *
   * @return the invalid {@code SpanId} lowercase in hex (base16) representation.
   */
  public static String getInvalid() {
    return INVALID;
  }

  /**
   * Returns whether the span identifier is valid. A valid span identifier is a 16 character hex
   * String, where at least one of the characters is not a '0'.
   *
   * @return {@code true} if the span identifier is valid.
   */
  public static boolean isValid(CharSequence spanId) {
    return (spanId.length() == HEX_SIZE)
        && !INVALID.contentEquals(spanId)
        && BigendianEncoding.isValidBase16String(spanId);
  }

  /**
   * Returns a {@code SpanId} built from a lowercase hex (base16) representation.
   *
   * @param src the lowercase hex (base16) representation.
   * @return a {@code SpanId} built from a lowercase hex (base16) representation.
   * @throws NullPointerException if {@code src} is null.
   * @throws IllegalArgumentException if not enough characters in the {@code src}.
   */
  public static byte[] bytesFromHex(CharSequence src) {
    return BigendianEncoding.bytesFromBase16(src, 0, HEX_SIZE);
  }

  /** Encode the bytes as hex (base16), padded with '0's on the left. */
  public static String bytesToHex(byte[] spanId) {
    return BigendianEncoding.toLowerBase16(spanId);
  }

  /** Generate a valid {@link SpanId} from the given long value. */
  public static String fromLong(long id) {
    char[] result = getTemporaryBuffer();
    BigendianEncoding.longToBase16String(id, result, 0);
    return new String(result);
  }

  /** Convert the the given hex spanId into a long representation. */
  public static long asLong(CharSequence src) {
    return BigendianEncoding.longFromBase16String(src, 0);
  }

  private static char[] getTemporaryBuffer() {
    char[] chars = charBuffer.get();
    if (chars == null) {
      chars = new char[HEX_SIZE];
      charBuffer.set(chars);
    }
    return chars;
  }
}
