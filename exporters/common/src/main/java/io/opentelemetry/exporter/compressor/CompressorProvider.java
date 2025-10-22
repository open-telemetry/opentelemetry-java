/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.compressor;

/** A service provider interface (SPI) for providing {@link Compressor}s. */
public interface CompressorProvider {

  /** Return the {@link Compressor}. */
  Compressor getInstance();
}
