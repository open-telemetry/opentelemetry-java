/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal.compressor;

import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.compression.CompressorProvider;

public class Base64CompressorProvider implements CompressorProvider {

  @Override
  public Compressor getInstance() {
    return Base64Compressor.getInstance();
  }
}
