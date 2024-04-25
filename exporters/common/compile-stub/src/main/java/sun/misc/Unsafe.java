/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package sun.misc;

import java.lang.reflect.Field;

/**
 * sun.misc.Unsafe from the JDK isn't found by the compiler, we provide out own trimmed down version
 * that we can compile against.
 */
public class Unsafe {

  public long objectFieldOffset(Field f) {
    return -1;
  }

  public Object getObject(Object o, long offset) {
    return null;
  }

  public byte getByte(Object o, long offset) {
    return 0;
  }

  public int arrayBaseOffset(Class<?> arrayClass) {
    return 0;
  }

  public long getLong(Object o, long offset) {
    return 0;
  }
}
