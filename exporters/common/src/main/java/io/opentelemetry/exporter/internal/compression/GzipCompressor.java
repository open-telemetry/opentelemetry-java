/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.compression;

import io.opentelemetry.exporter.compressor.Compressor;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Gzip {@link Compressor}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class GzipCompressor implements Compressor {

  public GzipCompressor() {}

  @Override
  public String getEncoding() {
    return "gzip";
  }

  @Override
  public OutputStream compress(OutputStream outputStream) throws IOException {
    return new GZIPOutputStream(outputStream);
  }
}
