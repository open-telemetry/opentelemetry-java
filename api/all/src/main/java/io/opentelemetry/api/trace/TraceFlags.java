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
  private static final int HEX_SIZE = 2 * SIZE;

  /** Returns the size in Hex of trace flags. */
  public static int getHexLength() {
    return HEX_SIZE;
  }

  /**
   * Returns the default byte representation of the flags.
   *
   * @return the default byte representation of the flags.
   */
  public static byte getDefault() {
    return DEFAULT;
  }

  /**
   * Returns the byte representation of the flags with the sampling bit set to {@code 1}.
   *
   * @return the byte representation of the flags with the sampling bit set to {@code 1}.
   */
  public static byte getSampled() {
    return IS_SAMPLED;
  }

  /** Extract the byte representation of the flags from a hex-representation. */
  public static byte byteFromHex(CharSequence src, int srcOffset) {
    return BigendianEncoding.byteFromBase16String(src, srcOffset);
  }
}
