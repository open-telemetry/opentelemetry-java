/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import javax.annotation.concurrent.Immutable;

/**
 * Helper methods for dealing with trace flags options. These options are propagated to all child
 * {@link Span spans}. These determine features such as whether a {@code Span} should be traced. It
 * is implemented as a bitmask.
 */
@Immutable
public final class TraceFlags {
  private TraceFlags() {}

  // Bit to represent whether trace is sampled or not.
  private static final byte IS_SAMPLED = 0x1;
  // the default flags are a 0 byte.
  private static final byte DEFAULT = 0x0;

  private static final int SIZE = 1;
  private static final int BASE16_SIZE = 2 * SIZE;

  /** Returns the size in Hex of trace flags. */
  public static int getHexLength() {
    return BASE16_SIZE;
  }

  /**
   * Returns the default {@code TraceFlags}.
   *
   * @return the default {@code TraceFlags}.
   */
  public static byte getDefault() {
    return DEFAULT;
  }

  /** Extract the sampled flag from hex-based trace-flags. */
  public static boolean isSampledFromHex(CharSequence src, int srcOffset) {
    // todo bypass the byte conversion and look directly at the hex.
    byte b = BigendianEncoding.byteFromBase16String(src, srcOffset);
    return (b & IS_SAMPLED) != 0;
  }

  /** Extract the byte representation of the flags from a hex-representation. */
  public static byte byteFromHex(CharSequence src, int srcOffset) {
    return BigendianEncoding.byteFromBase16String(src, srcOffset);
  }

  public static byte getSampled() {
    return IS_SAMPLED;
  }
}
