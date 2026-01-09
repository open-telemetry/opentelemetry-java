/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;

/**
 * This class contains shared logic for UTF-8 encoding operations while allowing subclasses to
 * implement different mechanisms for accessing String internal byte arrays (e.g., Unsafe vs
 * VarHandle).
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
abstract class AbstractStringEncoder implements StringEncoder {

  private final FallbackStringEncoder fallback = new FallbackStringEncoder();

  @Override
  public final void writeUtf8(CodedOutputStream output, String string, int utf8Length)
      throws IOException {
    // if the length of the latin1 string and the utf8 output are the same then the string must be
    // composed of only 7bit characters and can be directly copied to the output
    if (string.length() == utf8Length && isLatin1(string)) {
      byte[] bytes = getStringBytes(string);
      output.write(bytes, 0, bytes.length);
    } else {
      fallback.writeUtf8(output, string, utf8Length);
    }
  }

  @Override
  public final int getUtf8Size(String string) {
    if (isLatin1(string)) {
      byte[] bytes = getStringBytes(string);
      // latin1 bytes with negative value (most significant bit set) are encoded as 2 bytes in utf8
      return string.length() + countNegative(bytes);
    }

    return fallback.getUtf8Size(string);
  }

  protected abstract byte[] getStringBytes(String string);

  protected abstract boolean isLatin1(String string);

  protected abstract long getLong(byte[] bytes, int offset);

  // Inner loop can process at most 8 * 255 bytes without overflowing counter. To process more bytes
  // inner loop has to be run multiple times.
  private static final int MAX_INNER_LOOP_SIZE = 8 * 255;
  // mask that selects only the most significant bit in every byte of the long
  private static final long MOST_SIGNIFICANT_BIT_MASK = 0x8080808080808080L;

  /** Returns the count of bytes with negative value. */
  private int countNegative(byte[] bytes) {
    int count = 0;
    int offset = 0;
    // We are processing one long (8 bytes) at a time. In the inner loop we are keeping counts in a
    // long where each byte in the long is a separate counter. Due to this the inner loop can
    // process a maximum of 8*255 bytes at a time without overflow.
    for (int i = 1; i <= bytes.length / MAX_INNER_LOOP_SIZE + 1; i++) {
      long tmp = 0; // each byte in this long is a separate counter
      int limit = Math.min(i * MAX_INNER_LOOP_SIZE, bytes.length & ~7);
      for (; offset < limit; offset += 8) {
        long value = getLong(bytes, offset);
        // Mask the value keeping only the most significant bit in each byte and then shift this bit
        // to the position of the least significant bit in each byte. If the input byte was not
        // negative then after this transformation it will be zero, if it was negative then it will
        // be one.
        tmp += (value & MOST_SIGNIFICANT_BIT_MASK) >>> 7;
      }
      // sum up counts
      if (tmp != 0) {
        for (int j = 0; j < 8; j++) {
          count += (int) (tmp & 0xff);
          tmp = tmp >>> 8;
        }
      }
    }

    // Handle remaining bytes. Previous loop processes 8 bytes a time, if the input size is not
    // divisible with 8 the remaining bytes are handled here.
    for (int i = offset; i < bytes.length; i++) {
      // same as if (bytes[i] < 0) count++;
      count += bytes[i] >>> 31;
    }
    return count;
  }
}
