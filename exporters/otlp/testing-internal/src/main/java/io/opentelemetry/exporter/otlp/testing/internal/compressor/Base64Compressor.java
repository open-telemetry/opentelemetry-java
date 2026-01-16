/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal.compressor;

import io.opentelemetry.exporter.compressor.Compressor;
import java.io.OutputStream;
import java.util.Base64;

/**
 * This exists to test the compressor SPI mechanism but does not actually compress data in any
 * useful way.
 */
public final class Base64Compressor implements Compressor {

  public Base64Compressor() {}

  @Override
  public String getEncoding() {
    return "base64";
  }

  @Override
  public OutputStream compress(OutputStream outputStream) {
    return Base64.getEncoder().wrap(outputStream);
  }
}
