/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

/**
 * {@link ThreadLocal} buffers for use when creating new derived objects such as {@link String}s.
 * These buffers are reused within a single thread - it is _not safe_ to use the buffer to generate
 * multiple derived objects at the same time because the same memory will be used. In general, you
 * should get a temporary buffer, fill it with data, and finish by converting into the derived
 * object within the same method to avoid multiple usages of the same buffer.
 */
public final class TemporaryBuffers {

  private static final ThreadLocal<char[]> CHAR_ARRAY = new ThreadLocal<>();

  /**
   * A {@link ThreadLocal} {@code char[]} of size {@code len}. Take care when using a large value of
   * {@code len} as this buffer will remain for the lifetime of the thread. The returned buffer will
   * not be zeroed and may be larger than the requested size, you must make sure to fill the entire
   * content to the desired value and set the length explicitly when converting to a {@link String}.
   */
  public static char[] chars(int len) {
    char[] buffer = CHAR_ARRAY.get();
    if (buffer == null) {
      buffer = new char[len];
      CHAR_ARRAY.set(buffer);
    } else if (buffer.length < len) {
      buffer = new char[len];
      CHAR_ARRAY.set(buffer);
    }
    return buffer;
  }

  // Visible for testing
  static void clearChars() {
    CHAR_ARRAY.set(null);
  }

  private TemporaryBuffers() {}
}
