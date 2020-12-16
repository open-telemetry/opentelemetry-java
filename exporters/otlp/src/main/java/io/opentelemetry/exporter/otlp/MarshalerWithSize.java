/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

abstract class MarshalerWithSize implements Marshaler {
  private final int size;

  protected MarshalerWithSize(int size) {
    this.size = size;
  }

  @Override
  public final int getSerializedSize() {
    return size;
  }
}
