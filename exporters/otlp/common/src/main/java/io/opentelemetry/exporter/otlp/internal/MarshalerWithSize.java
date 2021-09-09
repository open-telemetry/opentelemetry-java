/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

abstract class MarshalerWithSize extends Marshaler {
  private final int size;

  protected MarshalerWithSize(int size) {
    this.size = size;
  }

  @Override
  public final int getBinarySerializedSize() {
    return size;
  }
}
