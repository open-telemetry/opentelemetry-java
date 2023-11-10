/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp;

import java.io.OutputStream;

class TestOutputStream extends OutputStream {
  private final int size;
  private int count;

  TestOutputStream() {
    this(-1);
  }

  TestOutputStream(int size) {
    this.size = size;
  }

  @Override
  public void write(int b) {
    count++;
    if (size > 0 && count > size) {
      throw new IllegalStateException("max size exceeded");
    }
  }
}
