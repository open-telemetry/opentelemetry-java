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
public class Base64Compressor implements Compressor {

  private static final Base64Compressor INSTANCE = new Base64Compressor();

  private Base64Compressor() {}

  public static Base64Compressor getInstance() {
    return INSTANCE;
  }

  @Override
  public String getEncoding() {
    return "base64";
  }

  @Override
  public OutputStream compress(OutputStream outputStream) {
    return Base64.getEncoder().wrap(outputStream);
  }
}
