/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.compression;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.stream.Collectors.joining;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import javax.annotation.Nullable;

/**
 * Utilities for resolving SPI {@link Compressor}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @see CompressorProvider
 */
public final class CompressorUtil {

  private static final Map<String, Compressor> compressorRegistry =
      buildCompressorRegistry(CompressorUtil.class.getClassLoader());

  private CompressorUtil() {}

  /**
   * Validate that the {@code compressionMethod} is "none" or matches a registered compressor.
   *
   * @return {@code null} if {@code compressionMethod} is "none" or the registered compressor
   * @throws IllegalArgumentException if no match is found
   */
  @Nullable
  public static Compressor validateAndResolveCompressor(String compressionMethod) {
    return validateAndResolveCompressor(compressionMethod, null);
  }

  /**
   * Validate that the {@code compressionMethod} is "none" or matches a registered compressor.
   *
   * @param compressionMethod the compression method to validate and resolve
   * @param classLoader the class loader to use for loading SPI implementations, or null to use the
   *     default
   * @return {@code null} if {@code compressionMethod} is "none" or the registered compressor
   * @throws IllegalArgumentException if no match is found
   */
  @Nullable
  public static Compressor validateAndResolveCompressor(
      String compressionMethod, @Nullable ClassLoader classLoader) {
    Map<String, Compressor> registry =
        classLoader == null ? compressorRegistry : buildCompressorRegistry(classLoader);

    Set<String> supportedEncodings = registry.keySet();
    Compressor compressor = registry.get(compressionMethod);
    checkArgument(
        "none".equals(compressionMethod) || compressor != null,
        "Unsupported compressionMethod. Compression method must be \"none\" or one of: "
            + supportedEncodings.stream().collect(joining(",", "[", "]")));
    return compressor;
  }

  private static Map<String, Compressor> buildCompressorRegistry(ClassLoader classLoader) {
    Map<String, Compressor> compressors = new HashMap<>();
    for (CompressorProvider spi : ServiceLoader.load(CompressorProvider.class, classLoader)) {
      Compressor compressor = spi.getInstance();
      compressors.put(compressor.getEncoding(), compressor);
    }
    // Hardcode gzip compressor
    compressors.put(GzipCompressor.getInstance().getEncoding(), GzipCompressor.getInstance());
    return compressors;
  }
}
