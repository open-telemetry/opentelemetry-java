/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.internal.OtelEncodingUtils;
import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
final class ImmutableTraceFlags implements TraceFlags {
  private static final ImmutableTraceFlags[] INSTANCES = buildInstances();
  // Bit to represent whether trace is sampled or not.
  private static final byte SAMPLED_BIT = 0x01;
  static final int CONTEXT_HAS_IS_REMOTE_BIT = 0x00000100;
  static final int CONTEXT_IS_REMOTE_BIT = 0x00000200;
  static final int CONTEXT_IS_REMOTE_MASK = CONTEXT_HAS_IS_REMOTE_BIT | CONTEXT_IS_REMOTE_BIT;

  static final ImmutableTraceFlags DEFAULT = fromByte((byte) 0x00);
  static final ImmutableTraceFlags SAMPLED = fromByte(SAMPLED_BIT);
  static final int HEX_LENGTH = 2;

  private final String hexRep;
  private final byte byteRep;

  // Implementation of the TraceFlags.fromHex().
  static ImmutableTraceFlags fromHex(CharSequence src, int srcOffset) {
    Objects.requireNonNull(src, "src");
    return fromByte(
        OtelEncodingUtils.byteFromBase16(src.charAt(srcOffset), src.charAt(srcOffset + 1)));
  }

  // Implementation of the TraceFlags.fromByte().
  static ImmutableTraceFlags fromByte(byte traceFlagsByte) {
    // Equivalent with Byte.toUnsignedInt(), but cannot use it because of Android.
    return INSTANCES[traceFlagsByte & 255];
  }

  private static ImmutableTraceFlags[] buildInstances() {
    ImmutableTraceFlags[] instances = new ImmutableTraceFlags[256];
    for (int i = 0; i < 256; i++) {
      instances[i] = new ImmutableTraceFlags((byte) i);
    }
    return instances;
  }

  private ImmutableTraceFlags(byte byteRep) {
    char[] result = new char[2];
    OtelEncodingUtils.byteToBase16(byteRep, result, 0);
    this.hexRep = new String(result);
    this.byteRep = byteRep;
  }

  @Override
  public boolean isSampled() {
    return (this.byteRep & SAMPLED_BIT) != 0;
  }

  @Override
  public String asHex() {
    return this.hexRep;
  }

  @Override
  public byte asByte() {
    return this.byteRep;
  }

  @Override
  public int withParentIsRemoteFlags(boolean isParentRemote) {
    if (isParentRemote) {
      return (this.byteRep & 0xff) | CONTEXT_IS_REMOTE_MASK;
    } else {
      return (this.byteRep & 0xff) | CONTEXT_HAS_IS_REMOTE_BIT;
    }
  }

  @Override
  public String toString() {
    return asHex();
  }
}
