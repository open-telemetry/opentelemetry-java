/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import javax.annotation.concurrent.Immutable;

/**
 * A valid trace flags is a byte or 2 character lowercase hex (base16) String.
 *
 * <p>These options are propagated to all child {@link Span spans}. These determine features such as
 * whether a {@code Span} should be traced.
 */
@Immutable
public interface TraceFlags {
  /**
   * Returns the default (with all flag bits off) byte representation of the {@link TraceFlags}.
   *
   * @return the default (with all flag bits off) byte representation of the {@link TraceFlags}.
   */
  static TraceFlags getDefault() {
    return ImmutableTraceFlags.DEFAULT;
  }

  /**
   * Returns the lowercase hex (base16) representation of the {@link TraceFlags} with the sampling
   * flag bit on.
   *
   * @return the lowercase hex (base16) representation of the {@link TraceFlags} with the sampling
   *     flag bit on.
   */
  static TraceFlags getSampled() {
    return ImmutableTraceFlags.SAMPLED;
  }

  /**
   * Returns the {@link TraceFlags} converted from the given two lowercase hex characters.
   *
   * @param first the first hex character.
   * @param second the second hex character.
   * @return the {@link TraceFlags} converted from the given lowercase hex (base16) representation.
   * @throws IllegalArgumentException if invalid lowercase hex characters.
   */
  static TraceFlags fromHex(char first, char second) {
    return fromByte(BigendianEncoding.byteFromBase16(first, second));
  }

  /**
   * Returns the {@link TraceFlags} converted from the given byte representation.
   *
   * @param traceFlagsByte the byte representation of the {@link TraceFlags}.
   * @return the {@link TraceFlags} converted from the given byte representation.
   */
  static TraceFlags fromByte(byte traceFlagsByte) {
    return ImmutableTraceFlags.fromByte(traceFlagsByte);
  }

  /**
   * Returns {@code true} if the sampling bit is on for this {@link TraceFlags}, otherwise {@code
   * false}.
   *
   * @return {@code true} if the sampling bit is on for this {@link TraceFlags}, otherwise {@code *
   *     false}.
   */
  boolean isSampled();

  /**
   * Returns the lowercase hex (base16) representation of this {@link TraceFlags}.
   *
   * @return the byte representation of the {@link TraceFlags}.
   */
  String asHex();

  /**
   * Returns the byte representation of this {@link TraceFlags}.
   *
   * @return the byte representation of the {@link TraceFlags}.
   */
  byte asByte();
}
