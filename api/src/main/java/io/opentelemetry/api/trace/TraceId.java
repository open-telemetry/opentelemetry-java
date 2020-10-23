/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/**
 * Helper methods for dealing with a trace identifier. A valid trace identifier is a 16-byte array
 * with at least one non-zero byte. In base-16 representation, a 32 character hex String, where at
 * least one of the characters is not a '0'.
 */
@Immutable
public final class TraceId {
  private static final ThreadLocal<char[]> charBuffer = new ThreadLocal<>();

  private static final int SIZE_IN_BYTES = 16;
  private static final int HEX_SIZE = 2 * BigendianEncoding.LONG_BASE16;
  private static final String INVALID = "00000000000000000000000000000000";

  private TraceId() {}

  /**
   * Returns the size in bytes of the {@code TraceId}.
   *
   * @return the size in bytes of the {@code TraceId}.
   */
  public static int getSize() {
    return SIZE_IN_BYTES;
  }

  /** Returns the length of the base16 (hex) representation of the {@code TraceId}. */
  public static int getHexLength() {
    return HEX_SIZE;
  }

  /**
   * Returns the invalid {@code TraceId}. All bytes are '\0'.
   *
   * @return the invalid {@code TraceId}.
   */
  public static String getInvalid() {
    return INVALID;
  }

  /**
   * Constructs a {@code TraceId} whose representation is specified by two long values representing
   * the lower and higher parts.
   *
   * <p>There is no restriction on the specified values, other than the already established validity
   * rules applying to {@code TraceId}. Specifying 0 for both values will effectively make the new
   * {@code TraceId} invalid.
   *
   * <p>This is equivalent to calling {@link #bytesToHex(byte[])} with the specified values stored
   * as big-endian.
   *
   * @param idHi the higher part of the {@code TraceId}.
   * @param idLo the lower part of the {@code TraceId}.
   */
  public static String fromLongs(long idHi, long idLo) {
    char[] chars = getTemporaryBuffer();
    BigendianEncoding.longToBase16String(idHi, chars, 0);
    BigendianEncoding.longToBase16String(idLo, chars, 16);
    return new String(chars);
  }

  private static char[] getTemporaryBuffer() {
    char[] chars = charBuffer.get();
    if (chars == null) {
      chars = new char[HEX_SIZE];
      charBuffer.set(chars);
    }
    return chars;
  }

  /**
   * Returns a {@code TraceId} built from a lowercase base16 representation.
   *
   * @param src the lowercase base16 representation.
   * @param srcOffset the offset in the buffer where the representation of the {@code TraceId}
   *     begins.
   * @return a {@code TraceId} built from a lowercase base16 representation.
   * @throws NullPointerException if {@code src} is null.
   * @throws IllegalArgumentException if not enough characters in the {@code src} from the {@code
   *     srcOffset}.
   */
  public static byte[] bytesFromHex(String src, int srcOffset) {
    Objects.requireNonNull(src, "src");
    return BigendianEncoding.bytesFromBase16(src, srcOffset, HEX_SIZE);
  }

  /**
   * Copies the lowercase base16 representations of the {@code TraceId} into the {@code dest}
   * beginning at the {@code destOffset} offset.
   *
   * @param dest the destination buffer.
   * @param destOffset the starting offset in the destination buffer.
   * @throws IndexOutOfBoundsException if {@code destOffset + 2 * TraceId.getSize()} is greater than
   *     {@code dest.length}.
   */
  public static void copyHexInto(byte[] traceId, char[] dest, int destOffset) {
    BigendianEncoding.longToBase16String(
        BigendianEncoding.longFromByteArray(traceId, 0), dest, destOffset);
    BigendianEncoding.longToBase16String(
        BigendianEncoding.longFromByteArray(traceId, 8), dest, destOffset + 16);
  }

  /**
   * Returns whether the {@code TraceId} is valid. A valid trace identifier is a 16-byte array with
   * at least one non-zero byte.
   *
   * @return {@code true} if the {@code TraceId} is valid.
   */
  public static boolean isValid(CharSequence traceId) {
    return (traceId.length() == HEX_SIZE)
        && !INVALID.contentEquals(traceId)
        && BigendianEncoding.isValidBase16String(traceId);
  }

  /**
   * Returns the lowercase base16 encoding of this {@code TraceId}.
   *
   * @return the lowercase base16 encoding of this {@code TraceId}.
   */
  public static String bytesToHex(byte[] traceId) {
    char[] chars = new char[HEX_SIZE];
    copyHexInto(traceId, chars, 0);
    return new String(chars);
  }

  /**
   * Returns the rightmost 8 bytes of the trace-id as a long value. This is used in
   * ProbabilitySampler.
   *
   * <p>This method is marked as internal and subject to change.
   *
   * @return the rightmost 8 bytes of the trace-id as a long value.
   */
  public static long getTraceIdRandomPart(CharSequence traceId) {
    return traceIdLowBytesAsLong(traceId);
  }

  /** Convert the "high bytes" of the given hex traceId into a long representation. */
  public static long traceIdHighBytesAsLong(CharSequence traceId) {
    return BigendianEncoding.longFromBase16String(traceId, 0);
  }

  /** Convert the "low bytes" of the given hex traceId into a long representation. */
  public static long traceIdLowBytesAsLong(CharSequence traceId) {
    return BigendianEncoding.longFromBase16String(traceId, BigendianEncoding.LONG_BASE16);
  }
}
