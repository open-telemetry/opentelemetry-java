/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.compression;

/**
 * A service provider interface (SPI) for providing {@link Compressor}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface CompressorProvider {

  /** Return the {@link Compressor}. */
  Compressor getInstance();
}
