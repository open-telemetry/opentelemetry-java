/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal.lz4;

import io.opentelemetry.exporter.internal.compression.Compressor;
import java.io.IOException;
import java.io.OutputStream;
import net.jpountz.lz4.LZ4FrameOutputStream;

public class Lz4Compressor implements Compressor {

  private static final Lz4Compressor INSTANCE = new Lz4Compressor();

  private Lz4Compressor() {}

  public static final Lz4Compressor getInstance() {
    return INSTANCE;
  }

  @Override
  public String getEncoding() {
    return "lz4";
  }

  @Override
  public OutputStream compress(OutputStream outputStream) throws IOException {
    return new LZ4FrameOutputStream(outputStream);
  }
}
