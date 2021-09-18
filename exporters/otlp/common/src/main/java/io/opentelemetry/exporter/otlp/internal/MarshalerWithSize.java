/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

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

  @Override
  public final int getBinarySerializedSize() {
    return size;
  }
}
