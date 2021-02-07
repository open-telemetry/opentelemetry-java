/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import javax.annotation.concurrent.Immutable;

/**
 * Helper methods for dealing with a span identifier. A valid span identifier is a 16 character
 * lowercase hex (base16) String, where at least one of the characters is not a "0".
 *
 * <p>There are two more other representation that this class helps with:
 *
 * <ul>
 *   <li>Bytes: a 8-byte array, where valid means that at least one of the bytes is not `\0`.
 *   <li>Long: a {@code long} value, where valid means that the value is non-zero.
 * </ul>
 */
@Immutable
public final class SpanId {
  private static final ThreadLocal<char[]> charBuffer = new ThreadLocal<>();

  private static final int HEX_LENGTH = 16;
  private static final String INVALID = "0000000000000000";

  private SpanId() {}

  /**
   * Returns the length of the lowercase hex (base16) representation of the {@code SpanId}.
   *
   * @return the length of the lowercase hex (base16) representation of the {@code SpanId}.
   */
  public static int getLength() {
    return HEX_LENGTH;
  }

  /**
   * Returns the invalid {@code SpanId} in lowercase hex (base16) representation. All characters are
   * "0".
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
    return (spanId.length() == HEX_LENGTH)
        && !INVALID.contentEquals(spanId)
        && BigendianEncoding.isValidBase16String(spanId);
  }

  /**
   * Returns the lowercase hex (base16) representation of the {@code SpanId} converted from the
   * given {@code long} value representation.
   *
   * <p>There is no restriction on the specified values, other than the already established validity
   * rules applying to {@code SpanId}. Specifying 0 for the long value will effectively return
   * {@link #getInvalid()}.
   *
   * @param id the higher part of the {@code TraceId}.
   * @return the lowercase hex (base16) representation of the {@code SpanId}.
   */
  public static String fromLong(long id) {
    if (id == 0) {
      return getInvalid();
    }
    char[] result = getTemporaryBuffer();
    BigendianEncoding.longToBase16String(id, result, 0);
    return new String(result);
  }

  private static char[] getTemporaryBuffer() {
    char[] chars = charBuffer.get();
    if (chars == null) {
      chars = new char[HEX_LENGTH];
      charBuffer.set(chars);
    }
    return chars;
  }
}
