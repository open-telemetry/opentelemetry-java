/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.marshal;

/**
 * A Marshaler which returns a memoized size.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public abstract class MarshalerWithSize extends Marshaler {
  private final int size;

  protected MarshalerWithSize(int size) {
    this.size = size;
  }

  /** Vendored {@link Byte#toUnsignedInt(byte)} to support Android. */
  protected static int toUnsignedInt(byte x) {
    return ((int) x) & 0xff;
  }

  @Override
  public final int getBinarySerializedSize() {
    return size;
  }
}
