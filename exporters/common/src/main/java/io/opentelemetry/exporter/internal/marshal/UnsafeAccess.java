/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.lang.reflect.Field;
import sun.misc.Unsafe;

class UnsafeAccess {
  private static final boolean available = checkUnsafe();

  static boolean isAvailable() {
    return available;
  }

  private static boolean checkUnsafe() {
    try {
      Class.forName("sun.misc.Unsafe", false, UnsafeAccess.class.getClassLoader());
      return UnsafeHolder.UNSAFE != null;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  static long objectFieldOffset(Field field) {
    return UnsafeHolder.UNSAFE.objectFieldOffset(field);
  }

  static Object getObject(Object object, long offset) {
    return UnsafeHolder.UNSAFE.getObject(object, offset);
  }

  static byte getByte(Object object, long offset) {
    return UnsafeHolder.UNSAFE.getByte(object, offset);
  }

  static int arrayBaseOffset(Class<?> arrayClass) {
    return UnsafeHolder.UNSAFE.arrayBaseOffset(arrayClass);
  }

  static long getLong(Object o, long offset) {
    return UnsafeHolder.UNSAFE.getLong(o, offset);
  }

  private UnsafeAccess() {}

  private static class UnsafeHolder {
    public static final Unsafe UNSAFE;

    static {
      UNSAFE = getUnsafe();
    }

    private UnsafeHolder() {}

    @SuppressWarnings("NullAway")
    private static Unsafe getUnsafe() {
      try {
        Field field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
      } catch (Exception ignored) {
        return null;
      }
    }
  }
}
