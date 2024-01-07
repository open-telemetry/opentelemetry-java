/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.compression;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Utilities for resolving SPI {@link Compressor}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @see CompressorProvider
 */
public final class CompressorUtil {

  private static final Map<String, Compressor> compressorRegistry = buildCompressorRegistry();

  private CompressorUtil() {}

  /** Get list of loaded compressors, named according to {@link Compressor#getEncoding()}. */
  public static Set<String> supportedCompressors() {
    return Collections.unmodifiableSet(compressorRegistry.keySet());
  }

  /**
   * Resolve the {@link Compressor} with the {@link Compressor#getEncoding()} equal to the {@code
   * encoding}.
   *
   * @throws IllegalArgumentException if no match is found
   */
  public static Compressor resolveCompressor(String encoding) {
    Compressor compressor = compressorRegistry.get(encoding);
    if (compressor == null) {
      throw new IllegalArgumentException(
          "Could not resolve compressor for encoding \"" + encoding + "\".");
    }
    return compressor;
  }

  private static Map<String, Compressor> buildCompressorRegistry() {
    Map<String, Compressor> compressors = new HashMap<>();
    for (CompressorProvider spi :
        ServiceLoader.load(CompressorProvider.class, CompressorUtil.class.getClassLoader())) {
      Compressor compressor = spi.getInstance();
      compressors.put(compressor.getEncoding(), compressor);
    }
    // Hardcode gzip compressor
    compressors.put(GzipCompressor.getInstance().getEncoding(), GzipCompressor.getInstance());
    return compressors;
  }
}
