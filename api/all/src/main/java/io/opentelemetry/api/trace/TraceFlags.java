/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/**
 * Helper methods for dealing with trace flags options. A valid trace flags is a 2 character
 * lowercase hex (base16) String.
 *
 * <p>These options are propagated to all child {@link Span spans}. These determine features such as
 * whether a {@code Span} should be traced. It is implemented as a bitmask.
 */
@Immutable
public final class TraceFlags {

  // Bit to represent whether trace is sampled or not.
  private static final byte SAMPLED_BIT = 0x1;

  private static final String DEFAULT = "00";
  private static final String SAMPLED = "01";

  private static final int HEX_LENGTH = 2;

  /**
   * Returns the length of the lowercase hex (base16) representation of the {@code TraceFlags}.
   *
   * @return the length of the lowercase hex (base16) representation of the {@code TraceFlags}.
   */
  public static int getLength() {
    return HEX_LENGTH;
  }

  /**
   * Returns the default (with all flag bits off) byte representation of the {@code TraceFlags}.
   *
   * @return the default (with all flag bits off) byte representation of the {@code TraceFlags}.
   */
  public static String getDefault() {
    return DEFAULT;
  }

  /**
   * Returns the lowercase hex (base16) representation of the {@code TraceFlags} with the sampling
   * flag bit on.
   *
   * @return the lowercase hex (base16) representation of the {@code TraceFlags} with the sampling
   *     flag bit on.
   */
  public static String getSampled() {
    return SAMPLED;
  }

  /**
   * Returns whether the span identifier is valid. A valid span identifier is a 16 character hex
   * String, where at least one of the characters is not a '0'.
   *
   * @return {@code true} if the span identifier is valid.
   */
  public static boolean isValid(CharSequence traceFlags) {
    return (traceFlags.length() == HEX_LENGTH) && BigendianEncoding.isValidBase16String(traceFlags);
  }

  /**
   * Returns the lowercase hex (base16) representation of the {@code TraceFlags} with the sampling
   * flag bit on.
   *
   * @return the lowercase hex (base16) representation of the {@code TraceFlags} with the sampling
   *     flag bit on.
   */
  public static boolean isSampled(String traceFlags) {
    // TODO: Improve this.
    return (asByte(traceFlags) & SAMPLED_BIT) != 0;
  }

  /** Extract the lowercase hex (base16) representation of the {@code TraceFlags} from a buffer. */
  public static String fromBuffer(CharSequence src, int srcOffset) {
    // TODO: Improve this by having all possible 256 strings pre-allocated.
    return fromByte(BigendianEncoding.byteFromBase16String(src, srcOffset));
  }

  /**
   * Returns the byte representation of the {@code TraceFlags} converted from the given lowercase
   * hex (base16) representation.
   *
   * @param traceFlagsByte the byte representation of the {@code TraceFlags}.
   * @return the lowercase hex (base16) representation of the {@code TraceFlags}.
   * @throws NullPointerException if {@code traceFlags} is null.
   * @throws IllegalArgumentException if not enough characters in the {@code traceFlags}.
   */
  public static String fromByte(byte traceFlagsByte) {
    return ALL_TRACE_FLAGS[traceFlagsByte & 0xFF].hex;
  }

  /**
   * Returns the byte representation of the {@code TraceFlags} converted from the given lowercase
   * hex (base16) representation.
   *
   * @param traceFlags the lowercase hex (base16) representation of the {@code TraceFlags}.
   * @return the byte representation of the {@code TraceFlags}.
   * @throws NullPointerException if {@code traceFlags} is null.
   * @throws IllegalArgumentException if not enough characters in the {@code traceFlags}.
   */
  public static byte asByte(CharSequence traceFlags) {
    Objects.requireNonNull(traceFlags, "traceFlags");
    return BigendianEncoding.byteFromBase16String(traceFlags, 0);
  }

  static TraceFlags parsedFromHex(String traceFlags) {
    return ALL_TRACE_FLAGS[asByte(traceFlags) & 0xFF];
  }

  private static final TraceFlags[] ALL_TRACE_FLAGS;

  static {
    TraceFlags[] allTraceFlags = new TraceFlags[256];
    for (int b = 0; b <= 255; b++) {
      allTraceFlags[b] = new TraceFlags((byte) b);
    }
    ALL_TRACE_FLAGS = allTraceFlags;
  }

  private final String hex;
  private final boolean sampled;

  private TraceFlags(byte flagsByte) {
    char[] result = new char[2];
    BigendianEncoding.byteToBase16String(flagsByte, result, 0);
    hex = new String(result);
    sampled = isSampled(hex);
  }

  boolean sampled() {
    return sampled;
  }

  String hex() {
    return hex;
  }
}
