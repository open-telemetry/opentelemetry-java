/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.internal.BigendianEncoding;
import javax.annotation.concurrent.Immutable;

/**
 * Helper methods for dealing with a trace identifier. A valid trace identifier is a 32 character
 * lowercase hex (base16) String, where at least one of the characters is not a "0".
 *
 * <p>There are two more other representation that this class helps with:
 *
 * <ul>
 *   <li>Bytes: a 16-byte array, where valid means that at least one of the bytes is not `\0`.
 *   <li>Long: two {@code long} values, where valid means that at least one of values is non-zero.
 *       To avoid allocating new objects this representation uses two parts, "high part"
 *       representing the left most part of the {@code TraceId} and "low part" representing the
 *       right most part of the {@code TraceId}. This is equivalent with the values being stored as
 *       big-endian.
 * </ul>
 */
@Immutable
public final class TraceId {
  private static final ThreadLocal<char[]> charBuffer = new ThreadLocal<>();

  private static final int HEX_LENGTH = 32;
  private static final String INVALID = "00000000000000000000000000000000";

  private TraceId() {}

  /**
   * Returns the length of the lowercase hex (base16) representation of the {@code TraceId}.
   *
   * @return the length of the lowercase hex (base16) representation of the {@code TraceId}.
   */
  public static int getLength() {
    return HEX_LENGTH;
  }

  /**
   * Returns the invalid {@code TraceId} in lowercase hex (base16) representation. All characters
   * are "0".
   *
   * @return the invalid {@code TraceId} in lowercase hex (base16) representation.
   */
  public static String getInvalid() {
    return INVALID;
  }

  /**
   * Returns whether the {@code TraceId} is valid. A valid trace identifier is a 32 character hex
   * String, where at least one of the characters is not a '0'.
   *
   * @return {@code true} if the {@code TraceId} is valid.
   */
  public static boolean isValid(CharSequence traceId) {
    return traceId != null
        && traceId.length() == HEX_LENGTH
        && !INVALID.contentEquals(traceId)
        && BigendianEncoding.isValidBase16String(traceId);
  }

  /**
   * Returns the lowercase hex (base16) representation of the {@code TraceId} converted from the
   * given bytes representation, or {@link #getInvalid()} if input is {@code null}.
   *
   * @param traceIdBytes the bytes (16-byte array) representation of the {@code TraceId}.
   * @return the lowercase hex (base16) representation of the {@code TraceId}.
   * @throws IndexOutOfBoundsException if {@code traceIdBytes} too short.
   */
  public static String fromBytes(byte[] traceIdBytes) {
    if (traceIdBytes == null) {
      return INVALID;
    }
    char[] result = getTemporaryBuffer();
    BigendianEncoding.bytesToBase16(traceIdBytes, result);
    return new String(result);
  }

  /**
   * Returns the bytes (16-byte array) representation of the {@code TraceId} converted from the
   * given two {@code long} values representing the lower and higher parts.
   *
   * <p>There is no restriction on the specified values, other than the already established validity
   * rules applying to {@code TraceId}. Specifying 0 for both values will effectively return {@link
   * #getInvalid()}.
   *
   * <p>This is equivalent to calling {@link #fromBytes(byte[])} with the specified values stored as
   * big-endian.
   *
   * @param traceIdLongHighPart the higher part of the long values representation of the {@code
   *     TraceId}.
   * @param traceIdLongLowPart the lower part of the long values representation of the {@code
   *     TraceId}.
   * @return the lowercase hex (base16) representation of the {@code TraceId}.
   */
  public static String fromLongs(long traceIdLongHighPart, long traceIdLongLowPart) {
    if (traceIdLongHighPart == 0 && traceIdLongLowPart == 0) {
      return getInvalid();
    }
    char[] chars = getTemporaryBuffer();
    BigendianEncoding.longToBase16String(traceIdLongHighPart, chars, 0);
    BigendianEncoding.longToBase16String(traceIdLongLowPart, chars, 16);
    return new String(chars);
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
