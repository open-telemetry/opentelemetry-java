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
 * whether a {@code Span} should be traced.
 */
@Immutable
public final class TraceFlags {
  private static final TraceFlags[] INSTANCES = buildInstances();
  // Bit to represent whether trace is sampled or not.
  private static final byte SAMPLED_BIT = 0x01;

  private static final TraceFlags DEFAULT = INSTANCES[byteToUnsignedInt((byte) 0x00)];
  private static final TraceFlags SAMPLED = INSTANCES[byteToUnsignedInt(SAMPLED_BIT)];

  private static final int HEX_LENGTH = 2;

  private final String hexRep;
  private final byte byteRep;

  /**
   * Returns the length of the lowercase hex (base16) representation of the {@link TraceFlags}.
   *
   * @return the length of the lowercase hex (base16) representation of the {@link TraceFlags}.
   */
  public static int getLength() {
    return HEX_LENGTH;
  }

  /**
   * Returns the default (with all flag bits off) byte representation of the {@link TraceFlags}.
   *
   * @return the default (with all flag bits off) byte representation of the {@link TraceFlags}.
   */
  public static TraceFlags getDefault() {
    return DEFAULT;
  }

  /**
   * Returns the lowercase hex (base16) representation of the {@link TraceFlags} with the sampling
   * flag bit on.
   *
   * @return the lowercase hex (base16) representation of the {@link TraceFlags} with the sampling
   *     flag bit on.
   */
  public static TraceFlags getSampled() {
    return SAMPLED;
  }

  /**
   * Returns the {@link TraceFlags} converted from the given lowercase hex (base16) representation.
   *
   * @param src the buffer where the hex (base16) representation of the {@link TraceFlags} is.
   * @param srcOffset the offset int buffer.
   * @return the {@link TraceFlags} converted from the given lowercase hex (base16) representation.
   * @throws NullPointerException if {@code src} is null.
   * @throws IndexOutOfBoundsException if {@code src} is too short.
   * @throws IllegalArgumentException if invalid characters in the {@code src}.
   */
  public static TraceFlags fromHex(CharSequence src, int srcOffset) {
    Objects.requireNonNull(src, "src");
    return INSTANCES[
        byteToUnsignedInt(
            BigendianEncoding.byteFromBase16(src.charAt(srcOffset), src.charAt(srcOffset + 1)))];
  }

  /**
   * Returns the {@link TraceFlags} converted from the given byte representation.
   *
   * @param traceFlagsByte the byte representation of the {@link TraceFlags}.
   * @return the {@link TraceFlags} converted from the given byte representation.
   */
  public static TraceFlags fromByte(byte traceFlagsByte) {
    return INSTANCES[byteToUnsignedInt(traceFlagsByte)];
  }

  private static TraceFlags[] buildInstances() {
    TraceFlags[] instances = new TraceFlags[256];
    for (int i = 0; i < 256; i++) {
      instances[i] = new TraceFlags((byte) i);
    }
    return instances;
  }

  private static int byteToUnsignedInt(byte x) {
    // Equivalent with Byte.toUnsignedInt(), but cannot use it because of Android.
    return x & 255;
  }

  private TraceFlags(byte byteRep) {
    char[] result = new char[2];
    BigendianEncoding.byteToBase16(byteRep, result, 0);
    this.hexRep = new String(result);
    this.byteRep = byteRep;
  }

  /**
   * Returns {@code true} if the sampling bit is on for this {@link TraceFlags}, otherwise {@code
   * false}.
   *
   * @return {@code true} if the sampling bit is on for this {@link TraceFlags}, otherwise {@code *
   *     false}.
   */
  public boolean isSampled() {
    return (this.byteRep & SAMPLED_BIT) != 0;
  }

  /**
   * Returns the lowercase hex (base16) representation of this {@link TraceFlags}.
   *
   * @return the byte representation of the {@link TraceFlags}.
   */
  public String asHex() {
    return this.hexRep;
  }

  /**
   * Returns the byte representation of this {@link TraceFlags}.
   *
   * @return the byte representation of the {@link TraceFlags}.
   */
  public byte asByte() {
    return this.byteRep;
  }

  @Override
  public String toString() {
    return asHex();
  }
}
