/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.trace;

import io.opentelemetry.internal.Utils;
import java.util.Random;
import javax.annotation.concurrent.Immutable;

/**
 * Helper methods for dealing with a trace identifier. A valid trace identifier is a 16-byte array
 * with at least one non-zero byte.
 *
 * @since 0.1.0
 */
@Immutable
public final class TraceId {
  private static final ThreadLocal<char[]> charBuffer = new ThreadLocal<>();

  private static final int SIZE_IN_BYTES = 16;
  private static final int BASE16_SIZE = 2 * BigendianEncoding.LONG_BASE16;
  private static final long INVALID_ID = 0;
  public static final String INVALID = "00000000000000000000000000000000";

  private TraceId() {}

  /**
   * Returns the size in bytes of the {@code TraceId}.
   *
   * @return the size in bytes of the {@code TraceId}.
   * @since 0.1.0
   */
  public static int getSize() {
    return SIZE_IN_BYTES;
  }

  /**
   * Returns the invalid {@code TraceId}. All bytes are '\0'.
   *
   * @return the invalid {@code TraceId}.
   * @since 0.1.0
   */
  public static CharSequence getInvalid() {
    return INVALID;
  }

  /**
   * Generates a new random {@code TraceId}.
   *
   * @param random the random number generator.
   * @return a new valid {@code TraceId}.
   */
  static CharSequence generateRandomId(Random random) {
    long idHi;
    long idLo;
    do {
      idHi = random.nextLong();
      idLo = random.nextLong();
    } while (idHi == INVALID_ID && idLo == INVALID_ID);
    return fromLongs(idHi, idLo);
  }

  /**
   * Constructs a {@code TraceId} whose representation is specified by two long values representing
   * the lower and higher parts.
   *
   * <p>There is no restriction on the specified values, other than the already established validity
   * rules applying to {@code TraceId}. Specifying 0 for both values will effectively make the new
   * {@code TraceId} invalid.
   *
   * <p>This is equivalent to calling {@link #toLowerBase16(byte[])} with the specified values
   * stored as big-endian.
   *
   * @param idHi the higher part of the {@code TraceId}.
   * @param idLo the lower part of the {@code TraceId}.
   * @since 0.1.0
   */
  public static CharSequence fromLongs(long idHi, long idLo) {
    char[] chars = getBuffer();
    BigendianEncoding.longToBase16String(idHi, chars, 0);
    BigendianEncoding.longToBase16String(idLo, chars, 16);
    return new String(chars);
  }

  private static char[] getBuffer() {
    char[] chars = charBuffer.get();
    if (chars == null) {
      chars = new char[BASE16_SIZE];
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
   * @since 0.1.0
   */
  public static byte[] bytesFromLowerBase16(CharSequence src, int srcOffset) {
    Utils.checkNotNull(src, "src");
    return BigendianEncoding.bytesFromBase16(src, srcOffset, BASE16_SIZE);
  }

  /**
   * Copies the lowercase base16 representations of the {@code TraceId} into the {@code dest}
   * beginning at the {@code destOffset} offset.
   *
   * @param dest the destination buffer.
   * @param destOffset the starting offset in the destination buffer.
   * @throws IndexOutOfBoundsException if {@code destOffset + 2 * TraceId.getSize()} is greater than
   *     {@code dest.length}.
   * @since 0.1.0
   */
  public static void copyLowerBase16Into(byte[] traceId, char[] dest, int destOffset) {
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
   * @since 0.1.0
   */
  public static boolean isValid(CharSequence traceId) {
    return (traceId.length() == BASE16_SIZE)
        && !INVALID.contentEquals(traceId)
        && BigendianEncoding.isValidBase16String(traceId);
  }

  /**
   * Returns the lowercase base16 encoding of this {@code TraceId}.
   *
   * @return the lowercase base16 encoding of this {@code TraceId}.
   * @since 0.1.0
   */
  public static CharSequence toLowerBase16(byte[] traceId) {
    char[] chars = new char[BASE16_SIZE];
    copyLowerBase16Into(traceId, chars, 0);
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
    return BigendianEncoding.longFromBase16String(traceId, BigendianEncoding.LONG_BASE16);
  }
}
