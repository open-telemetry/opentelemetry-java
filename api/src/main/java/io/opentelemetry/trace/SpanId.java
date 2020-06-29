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

import java.util.Random;
import javax.annotation.concurrent.Immutable;

/**
 * Helper methods for dealing with a span identifier. A valid span identifier is an 8-byte array
 * with at least one non-zero byte.
 *
 * @since 0.1.0
 */
@Immutable
public final class SpanId {
  private static final ThreadLocal<char[]> charBuffer = new ThreadLocal<>();

  private SpanId() {}

  private static final int SIZE = 8;
  private static final int BASE16_SIZE = 2 * SIZE;
  private static final long INVALID_ID = 0;
  public static final String INVALID = "0000000000000000";

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
  public static CharSequence getInvalid() {
    return INVALID;
  }

  /**
   * Generates a new random {@code SpanId}.
   *
   * @param random The random number generator.
   * @return a valid new {@code SpanId}.
   */
  static CharSequence generateRandomId(Random random) {
    long id;
    do {
      id = random.nextLong();
    } while (id == INVALID_ID);
    return fromLong(id);
  }

  /** Converts the long id value into a base-16 representation of it. */
  public static CharSequence fromLong(long id) {
    char[] chars = getBuffer();
    BigendianEncoding.longToBase16String(id, chars, 0);
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
  public static byte[] bytesFromLowerBase16(CharSequence src, int srcOffset) {
    return BigendianEncoding.bytesFromBase16(src, srcOffset, BASE16_SIZE);
  }

  /**
   * Returns whether the span identifier is valid. A valid span identifier is an 8-byte array with
   * at least one non-zero byte.
   *
   * @return {@code true} if the span identifier is valid.
   * @since 0.1.0
   */
  public static boolean isValid(CharSequence spanId) {
    return (spanId.length() == BASE16_SIZE)
        && !INVALID.contentEquals(spanId)
        && BigendianEncoding.isValidBase16String(spanId);
  }

  /** Encode the bytes as base-16 (hex), padded with '0's on the left. */
  public static CharSequence toLowerBase16(byte[] spanId) {
    return BigendianEncoding.toLowerBase16(spanId);
  }
}
