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
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A class that represents a trace identifier. A valid trace identifier is a 16-byte array with at
 * least one non-zero byte.
 *
 * @since 0.1.0
 */
@Immutable
public final class TraceId implements Comparable<TraceId> {

  private static final int SIZE = 16;
  private static final int BASE16_SIZE = 2 * BigendianEncoding.LONG_BASE16;
  private static final long INVALID_ID = 0;
  private static final TraceId INVALID = new TraceId(INVALID_ID, INVALID_ID);

  // The internal representation of the TraceId.
  private final long idHi;
  private final long idLo;

  /**
   * Constructs a {@code TraceId} whose representation is specified by two long values representing
   * the lower and higher parts.
   *
   * <p>There is no restriction on the specified values, other than the already established validity
   * rules applying to {@code TraceId}. Specifying 0 for both values will effectively make the new
   * {@code TraceId} invalid.
   *
   * <p>This is equivalent to calling {@link #fromBytes(byte[], int)} with the specified values
   * stored as big-endian.
   *
   * @param idHi the higher part of the {@code TraceId}.
   * @param idLo the lower part of the {@code TraceId}.
   * @since 0.1.0
   */
  public TraceId(long idHi, long idLo) {
    this.idHi = idHi;
    this.idLo = idLo;
  }

  /**
   * Returns the size in bytes of the {@code TraceId}.
   *
   * @return the size in bytes of the {@code TraceId}.
   * @since 0.1.0
   */
  public static int getSize() {
    return SIZE;
  }

  /**
   * Returns the invalid {@code TraceId}. All bytes are '\0'.
   *
   * @return the invalid {@code TraceId}.
   * @since 0.1.0
   */
  public static TraceId getInvalid() {
    return INVALID;
  }

  /**
   * Generates a new random {@code TraceId}.
   *
   * @param random the random number generator.
   * @return a new valid {@code TraceId}.
   */
  static TraceId generateRandomId(Random random) {
    long idHi;
    long idLo;
    do {
      idHi = random.nextLong();
      idLo = random.nextLong();
    } while (idHi == INVALID_ID && idLo == INVALID_ID);
    return new TraceId(idHi, idLo);
  }

  /**
   * Returns a {@code TraceId} whose representation is copied from the {@code src} beginning at the
   * {@code srcOffset} offset.
   *
   * @param src the buffer where the representation of the {@code TraceId} is copied.
   * @param srcOffset the offset in the buffer where the representation of the {@code TraceId}
   *     begins.
   * @return a {@code TraceId} whose representation is copied from the buffer.
   * @throws NullPointerException if {@code src} is null.
   * @throws IndexOutOfBoundsException if {@code srcOffset+TraceId.getSize()} is greater than {@code
   *     src.length}.
   * @since 0.1.0
   */
  public static TraceId fromBytes(byte[] src, int srcOffset) {
    Utils.checkNotNull(src, "src");
    return new TraceId(
        BigendianEncoding.longFromByteArray(src, srcOffset),
        BigendianEncoding.longFromByteArray(src, srcOffset + BigendianEncoding.LONG_BYTES));
  }

  /**
   * Copies the byte array representations of the {@code TraceId} into the {@code dest} beginning at
   * the {@code destOffset} offset.
   *
   * @param dest the destination buffer.
   * @param destOffset the starting offset in the destination buffer.
   * @throws NullPointerException if {@code dest} is null.
   * @throws IndexOutOfBoundsException if {@code destOffset+TraceId.getSize()} is greater than
   *     {@code dest.length}.
   * @since 0.1.0
   */
  public void copyBytesTo(byte[] dest, int destOffset) {
    BigendianEncoding.longToByteArray(idHi, dest, destOffset);
    BigendianEncoding.longToByteArray(idLo, dest, destOffset + BigendianEncoding.LONG_BYTES);
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
  public static TraceId fromLowerBase16(CharSequence src, int srcOffset) {
    Utils.checkNotNull(src, "src");
    return new TraceId(
        BigendianEncoding.longFromBase16String(src, srcOffset),
        BigendianEncoding.longFromBase16String(src, srcOffset + BigendianEncoding.LONG_BASE16));
  }

  /**
   * Transforms a byte[] representation of a trace id (eg. from a protobuf) and returns a base 16
   * representation of the same.
   *
   * @param bytes The byte array containing the trace id.
   * @return A base 16 representation of the trace id.
   * @since 0.1.0
   */
  public static String asBase16(byte[] bytes) {
    long idHi = BigendianEncoding.longFromByteArray(bytes, 0);
    long idLo = BigendianEncoding.longFromByteArray(bytes, SIZE / 2);
    char[] chars = new char[BASE16_SIZE];
    BigendianEncoding.longToBase16String(idHi, chars, 0);
    BigendianEncoding.longToBase16String(idLo, chars, BASE16_SIZE / 2);
    return new String(chars);
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
  public void copyLowerBase16To(char[] dest, int destOffset) {
    BigendianEncoding.longToBase16String(idHi, dest, destOffset);
    BigendianEncoding.longToBase16String(idLo, dest, destOffset + BASE16_SIZE / 2);
  }

  /**
   * Returns whether the {@code TraceId} is valid. A valid trace identifier is a 16-byte array with
   * at least one non-zero byte.
   *
   * @return {@code true} if the {@code TraceId} is valid.
   * @since 0.1.0
   */
  public boolean isValid() {
    return idHi != INVALID_ID || idLo != INVALID_ID;
  }

  /**
   * Returns the lowercase base16 encoding of this {@code TraceId}.
   *
   * @return the lowercase base16 encoding of this {@code TraceId}.
   * @since 0.1.0
   */
  public String toLowerBase16() {
    char[] chars = new char[BASE16_SIZE];
    copyLowerBase16To(chars, 0);
    return new String(chars);
  }

  @Override
  public boolean equals(@Nullable Object obj) {
    if (obj == this) {
      return true;
    }

    if (!(obj instanceof TraceId)) {
      return false;
    }

    TraceId that = (TraceId) obj;
    return idHi == that.idHi && idLo == that.idLo;
  }

  /**
   * Returns the lower 8 bytes of the trace-id as a long value, assuming little-endian order. This
   * is used in ProbabilitySampler.
   *
   * <p>This method is marked as internal and subject to change.
   *
   * @return the lower 8 bytes of the trace-id as a long value, assuming little-endian order.
   */
  public long getLowerLong() {
    return (idHi < 0) ? -idHi : idHi;
  }

  @Override
  public int hashCode() {
    // Copied from Arrays.hashCode(long[])
    int result = 1;
    result = 31 * result + ((int) (idHi ^ (idHi >>> 32)));
    result = 31 * result + ((int) (idLo ^ (idLo >>> 32)));
    return result;
  }

  @Override
  public String toString() {
    return "TraceId{traceId=" + toLowerBase16() + "}";
  }

  @Override
  public int compareTo(TraceId that) {
    if (idHi == that.idHi) {
      if (idLo == that.idLo) {
        return 0;
      }
      return idLo < that.idLo ? -1 : 1;
    }
    return idHi < that.idHi ? -1 : 1;
  }
}
