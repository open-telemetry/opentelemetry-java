/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal.lz4;

import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.compression.CompressorProvider;

public class Lz4CompressorProvider implements CompressorProvider {

  @Override
  public Compressor getInstance() {
    return Lz4Compressor.getInstance();
  }
}
