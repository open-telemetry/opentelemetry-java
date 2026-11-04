/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.impl;

import io.opentelemetry.api.internal.OtelEncodingUtils;

/**
 * Base16 encoding and decoding utilities.
 *
 * <p>This class is not intended for use by application developers. Its API is stable and will not
 * be changed or removed in a backwards-incompatible manner.
 */
public final class Base16EncodingUtils {

  /**
   * Returns the {@code long} value whose base16 representation is stored in the first 16 chars of
   * {@code chars} starting from the {@code offset}.
   *
   * @param chars the base16 representation of the {@code long}.
   * @param offset the starting offset in the {@code CharSequence}.
   */
  public static long longFromBase16String(CharSequence chars, int offset) {
    return OtelEncodingUtils.longFromBase16String(chars, offset);
  }

  /**
   * Appends the base16 encoding of the specified {@code value} to the {@code dest}.
   *
   * @param value the value to be converted.
   * @param dest the destination char array.
   * @param destOffset the starting offset in the destination char array.
   */
  public static void longToBase16String(long value, char[] dest, int destOffset) {
    OtelEncodingUtils.longToBase16String(value, dest, destOffset);
  }

  /** Returns the {@code byte[]} decoded from the given hex {@link CharSequence}. */
  public static byte[] bytesFromBase16(CharSequence value, int length) {
    return OtelEncodingUtils.bytesFromBase16(value, length);
  }

  /** Fills {@code bytes} with bytes decoded from the given hex {@link CharSequence}. */
  public static void bytesFromBase16(CharSequence value, int length, byte[] bytes) {
    OtelEncodingUtils.bytesFromBase16(value, length, bytes);
  }

  /** Returns whether the {@link CharSequence} is a valid hex string. */
  public static boolean isValidBase16String(CharSequence value) {
    return OtelEncodingUtils.isValidBase16String(value);
  }

  private Base16EncodingUtils() {}
}
