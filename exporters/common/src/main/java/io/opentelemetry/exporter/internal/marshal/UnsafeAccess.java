/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import io.opentelemetry.api.internal.ConfigUtil;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

class UnsafeAccess {
  private static final int MAX_ENABLED_JAVA_VERSION = 22;
  private static final boolean available = checkUnsafe();

  static boolean isAvailable() {
    return available;
  }

  private static boolean checkUnsafe() {
    double javaVersion = getJavaVersion();
    boolean unsafeEnabled =
        Boolean.parseBoolean(
            ConfigUtil.getString(
                "otel.java.experimental.exporter.unsafe.enabled",
                javaVersion != -1 && javaVersion <= MAX_ENABLED_JAVA_VERSION ? "true" : "false"));
    if (!unsafeEnabled) {
      return false;
    }

    try {
      Class.forName("sun.misc.Unsafe", false, UnsafeAccess.class.getClassLoader());
      return UnsafeHolder.UNSAFE != null;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  private static double getJavaVersion() {
    String specVersion = System.getProperty("java.specification.version");
    if (specVersion != null) {
      try {
        return Double.parseDouble(specVersion);
      } catch (NumberFormatException exception) {
        // ignore
      }
    }
    return -1;
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
    private static final Unsafe UNSAFE;

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
