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
 * A class that represents a span identifier. A valid span identifier is an 8-byte array with at
 * least one non-zero byte.
 *
 * @since 0.1.0
 */
@Immutable
public final class SpanId implements Comparable<SpanId> {

  private static final int SIZE = 8;
  private static final int BASE16_SIZE = 2 * SIZE;
  private static final long INVALID_ID = 0;
  private static final SpanId INVALID = new SpanId(INVALID_ID);

  // The internal representation of the SpanId.
  private final long id;

  /**
   * Constructs a {@code SpanId} whose representation is specified by a long value.
   *
   * <p>There is no restriction on the specified value, other than the already established validity
   * rules applying to {@code SpanId}. Specifying 0 for this value will effectively make the new
   * {@code SpanId} invalid.
   *
   * <p>This is equivalent to calling {@link #fromBytes(byte[], int)} with the specified value
   * stored as big-endian.
   *
   * @param id the long representation of the {@code TraceId}.
   * @since 0.1.0
   */
  public SpanId(long id) {
    this.id = id;
  }

  /**
   * Returns the size in bytes of the {@code SpanId}.
   *
   * @return the size in bytes of the {@code SpanId}.
   * @since 0.1.0
   */
  public static int getSize() {
    return SIZE;
  }

  /**
   * Returns the invalid {@code SpanId}. All bytes are 0.
   *
   * @return the invalid {@code SpanId}.
   * @since 0.1.0
   */
  public static SpanId getInvalid() {
    return INVALID;
  }

  /**
   * Generates a new random {@code SpanId}.
   *
   * @param random The random number generator.
   * @return a valid new {@code SpanId}.
   */
  static SpanId generateRandomId(Random random) {
    long id;
    do {
      id = random.nextLong();
    } while (id == INVALID_ID);
    return new SpanId(id);
  }

  /**
   * Returns a {@code SpanId} whose representation is copied from the {@code src} beginning at the
   * {@code srcOffset} offset.
   *
   * @param src the buffer where the representation of the {@code SpanId} is copied.
   * @param srcOffset the offset in the buffer where the representation of the {@code SpanId}
   *     begins.
   * @return a {@code SpanId} whose representation is copied from the buffer.
   * @throws NullPointerException if {@code src} is null.
   * @throws IndexOutOfBoundsException if {@code srcOffset+SpanId.getSize()} is greater than {@code
   *     src.length}.
   * @since 0.1.0
   */
  public static SpanId fromBytes(byte[] src, int srcOffset) {
    Utils.checkNotNull(src, "src");
    return new SpanId(BigendianEncoding.longFromByteArray(src, srcOffset));
  }

  /**
   * Transforms a byte[] representation of a span id (eg. from a protobuf) and returns a base 16
   * representation of the same.
   *
   * @param bytes The byte array containing the span id.
   * @return A base 16 representation of the span id.
   * @since 0.1.0
   */
  public static String asLowerBase16(byte[] bytes) {
    long longId = BigendianEncoding.longFromByteArray(bytes, 0);
    char[] chars = new char[BASE16_SIZE];
    BigendianEncoding.longToBase16String(longId, chars, 0);
    return new String(chars);
  }

  /**
   * Copies the byte array representations of the {@code SpanId} into the {@code dest} beginning at
   * the {@code destOffset} offset.
   *
   * @param dest the destination buffer.
   * @param destOffset the starting offset in the destination buffer.
   * @throws NullPointerException if {@code dest} is null.
   * @throws IndexOutOfBoundsException if {@code destOffset+SpanId.getSize()} is greater than {@code
   *     dest.length}.
   * @since 0.1.0
   */
  public void copyBytesTo(byte[] dest, int destOffset) {
    BigendianEncoding.longToByteArray(id, dest, destOffset);
  }

  /**
   * Returns a {@code SpanId} built from a lowercase base16 representation.
   *
   * @param src the lowercase base16 representation.
   * @param srcOffset the offset in the buffer where the representation of the {@code SpanId}
   *     begins.
   * @return a {@code SpanId} built from a lowercase base16 representation.
   * @throws NullPointerException if {@code src} is null.
   * @throws IllegalArgumentException if not enough characters in the {@code src} from the {@code
   *     srcOffset}.
   * @since 0.1.0
   */
  public static SpanId fromLowerBase16(CharSequence src, int srcOffset) {
    Utils.checkNotNull(src, "src");
    return new SpanId(BigendianEncoding.longFromBase16String(src, srcOffset));
  }

  /**
   * Copies the lowercase base16 representations of the {@code SpanId} into the {@code dest}
   * beginning at the {@code destOffset} offset.
   *
   * @param dest the destination buffer.
   * @param destOffset the starting offset in the destination buffer.
   * @throws IndexOutOfBoundsException if {@code destOffset + 2 * SpanId.getSize()} is greater than
   *     {@code dest.length}.
   * @since 0.1.0
   */
  public void copyLowerBase16To(char[] dest, int destOffset) {
    BigendianEncoding.longToBase16String(id, dest, destOffset);
  }

  /**
   * Returns whether the span identifier is valid. A valid span identifier is an 8-byte array with
   * at least one non-zero byte.
   *
   * @return {@code true} if the span identifier is valid.
   * @since 0.1.0
   */
  public boolean isValid() {
    return id != INVALID_ID;
  }

  /**
   * Returns the lowercase base16 encoding of this {@code SpanId}.
   *
   * @return the lowercase base16 encoding of this {@code SpanId}.
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

    if (!(obj instanceof SpanId)) {
      return false;
    }

    SpanId that = (SpanId) obj;
    return id == that.id;
  }

  @Override
  public int hashCode() {
    // Copied from Long.hashCode in java8.
    return (int) (id ^ (id >>> 32));
  }

  @Override
  public String toString() {
    return "SpanId{spanId=" + toLowerBase16() + "}";
  }

  @Override
  public int compareTo(SpanId that) {
    // Copied from Long.compare in java8.
    return (id < that.id) ? -1 : ((id == that.id) ? 0 : 1);
  }
}
