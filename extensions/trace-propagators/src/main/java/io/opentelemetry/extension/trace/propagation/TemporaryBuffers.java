/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

/**
 * Provides reusable thread-local buffers for creating values such as {@link String}s. Calls on the
 * same thread may return the same array, so finish writing and consuming the required characters
 * before reusing the buffer. Do not assume the buffer is cleared between calls.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class TemporaryBuffers {

  private static final ThreadLocal<char[]> CHAR_ARRAY = new ThreadLocal<>();

  /**
   * Returns a thread-local {@code char[]} with capacity of at least {@code len}.
   *
   * <p>The returned buffer may be reused, is not cleared, and may be larger than {@code len}. Set
   * every character that will be included in the result, and specify the intended length when
   * converting it to a {@link String}. Avoid requesting unnecessarily large buffers because they
   * remain associated with the thread.
   *
   * @param len the minimum buffer capacity
   * @return a reusable thread-local character buffer with capacity of at least {@code len}
   */
  static char[] chars(int len) {
    char[] buffer = CHAR_ARRAY.get();
    if (buffer == null || buffer.length < len) {
      buffer = new char[len];
      CHAR_ARRAY.set(buffer);
    }
    return buffer;
  }

  private TemporaryBuffers() {}
}
