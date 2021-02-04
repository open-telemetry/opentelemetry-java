/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import javax.annotation.concurrent.Immutable;

/**
 * Helper methods for dealing with trace flags options. A valid trace flags is a byte.
 *
 * <p>These options are propagated to all child {@link Span spans}. These determine features such as
 * whether a {@code Span} should be traced. It is implemented as a bitmask.
 *
 * <p>There is another representation that this class helps with:
 *
 * <ul>
 *   <li>Hex: 2 lowercase hex (base16) characters.
 * </ul>
 */
@Immutable
public final class TraceFlags {
  private TraceFlags() {}

  // Bit to represent whether trace is sampled or not.
  private static final byte SAMPLED_BIT = 0x1;
  private static final byte DEFAULT = 0x0;
  private static final int SIZE = 1;

  /**
   * Returns the length of byte representation of the {@code TraceFlags}.
   *
   * @return the length of byte representation of the {@code TraceFlags}.
   */
  public static int getLength() {
    return SIZE;
  }

  /**
   * Returns the default (with all flag bits off) byte representation of the {@code TraceFlags}.
   *
   * @return the default (with all flag bits off) byte representation of the {@code TraceFlags}.
   */
  public static byte getDefault() {
    return DEFAULT;
  }

  /**
   * Returns the byte representation of the {@code TraceFlags} with the sampling flag bit on.
   *
   * @return the byte representation of the {@code TraceFlags} with the sampling flag bit on.
   */
  public static byte getSampled() {
    return SAMPLED_BIT;
  }

  /**
   * Returns {@code true} if the sampling bit is on for this byte representation of the {@code
   * TraceFlags}, otherwise {@code false}.
   *
   * @param traceFlags the byte representation of the {@code TraceFlags}.
   * @return {@code true} if the sampling bit is on for this byte representation of the {@code *
   *     TraceFlags}, otherwise {@code false}.
   */
  public static boolean isSampled(byte traceFlags) {
    return (traceFlags & SAMPLED_BIT) != 0;
  }

  /**
   * Returns the byte representation of the {@code TraceFlags} converted from the given hex (base16)
   * representation.
   *
   * @param traceFlagsHex the lowercase hex (base16) representation of the {@code TraceFlags}.
   * @param offset the offset in the {@code traceFlagsHex} where the {@code TraceFlags} begin.
   * @return the byte representation of the {@code TraceFlags}.
   * @throws NullPointerException if {@code traceFlagsHex} is null.
   * @throws IllegalArgumentException if not enough characters in the {@code traceFlagsHex}.
   */
  public static byte fromHex(CharSequence traceFlagsHex, int offset) {
    return BigendianEncoding.byteFromBase16String(traceFlagsHex, offset);
  }

  /**
   * Copies lowercase hex (base16) representation of the {@code TraceFlags} converted from the given
   * hex (base16) representation.
   *
   * @param traceFlags the byte representation of the {@code TraceFlags}.
   * @param dest the destination buffer.
   * @param destOffset the offset in the destination buffer.
   * @throws IllegalArgumentException if not enough characters in the {@code dest}.
   */
  public static void copyAsHex(byte traceFlags, char[] dest, int destOffset) {
    BigendianEncoding.byteToBase16String(traceFlags, dest, destOffset);
  }
}
