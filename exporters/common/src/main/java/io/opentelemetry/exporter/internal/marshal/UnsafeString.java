/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.lang.reflect.Field;

class UnsafeString {
  private static final long valueOffset = getStringFieldOffset("value", byte[].class);
  private static final long coderOffset = getStringFieldOffset("coder", byte.class);
  private static final int byteArrayBaseOffset =
      UnsafeAccess.isAvailable() ? UnsafeAccess.arrayBaseOffset(byte[].class) : -1;
  private static final boolean available = valueOffset != -1 && coderOffset != -1;

  static boolean isAvailable() {
    return available;
  }

  static boolean isLatin1(String string) {
    // 0 represents latin1, 1 utf16
    return UnsafeAccess.getByte(string, coderOffset) == 0;
  }

  static byte[] getBytes(String string) {
    return (byte[]) UnsafeAccess.getObject(string, valueOffset);
  }

  static long getLong(byte[] bytes, int index) {
    return UnsafeAccess.getLong(bytes, byteArrayBaseOffset + index);
  }

  private static long getStringFieldOffset(String fieldName, Class<?> expectedType) {
    if (!UnsafeAccess.isAvailable()) {
      return -1;
    }

    try {
      Field field = String.class.getDeclaredField(fieldName);
      if (field.getType() != expectedType) {
        return -1;
      }
      return UnsafeAccess.objectFieldOffset(field);
    } catch (Exception exception) {
      return -1;
    }
  }

  private UnsafeString() {}
}
