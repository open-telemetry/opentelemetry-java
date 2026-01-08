/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

import java.io.IOException;

/**
 * Interface for efficient UTF-8 string encoding operations.
 *
 * <p>This interface provides optimized string-to-UTF-8 conversion with multiple implementations
 * based on available platform capabilities.
 *
 * <p>The optimal implementation is automatically selected at runtime via {@link #getInstance()}, in
 * this priority order:
 *
 * <ul>
 *   <li>{@code VarHandleStringEncoder} - High-performance Java 9+ implementation using VarHandle
 *   <li>{@code UnsafeStringEncoder} - High-performance Java 8+ implementation using sun.misc.Unsafe
 *   <li>{@code FallbackStringEncoder} - Implementation using standard Java operations
 * </ul>
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
// public visibility only needed for benchmarking purposes
public interface StringEncoder {

  /** Returns the number of bytes required to encode the string as UTF-8. */
  int getUtf8Size(String string);

  /**
   * Write a string as UTF-8 bytes to the output stream using the pre-calculated UTF-8 length from
   * {@link #getUtf8Size(String)}.
   */
  void writeUtf8(CodedOutputStream output, String string, int utf8Length) throws IOException;

  /** Returns the best available StringEncoder implementation. */
  static StringEncoder getInstance() {
    return StringEncoderHolder.INSTANCE;
  }
}
