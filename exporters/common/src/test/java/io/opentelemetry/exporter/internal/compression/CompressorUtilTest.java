/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.compression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.api.Test;

class CompressorUtilTest {

  @Test
  void validateAndResolveCompressor_none() {
    assertThat(CompressorUtil.validateAndResolveCompressor("none")).isNull();
  }

  @Test
  void validateAndResolveCompressor_gzip() {
    assertThat(CompressorUtil.validateAndResolveCompressor("gzip"))
        .isEqualTo(GzipCompressor.getInstance());
  }

  @Test
  void validateAndResolveCompressor_invalid() {
    assertThatThrownBy(() -> CompressorUtil.validateAndResolveCompressor("invalid"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported compressionMethod");
  }

  @Test
  void validateAndResolveCompressor_withClassLoader_none() {
    ClassLoader classLoader = CompressorUtilTest.class.getClassLoader();
    assertThat(CompressorUtil.validateAndResolveCompressor("none", classLoader)).isNull();
  }

  @Test
  void validateAndResolveCompressor_withClassLoader_gzip() {
    ClassLoader classLoader = CompressorUtilTest.class.getClassLoader();
    assertThat(CompressorUtil.validateAndResolveCompressor("gzip", classLoader))
        .isEqualTo(GzipCompressor.getInstance());
  }

  @Test
  void validateAndResolveCompressor_withClassLoader_invalid() {
    ClassLoader classLoader = CompressorUtilTest.class.getClassLoader();
    assertThatThrownBy(() -> CompressorUtil.validateAndResolveCompressor("invalid", classLoader))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported compressionMethod");
  }

  @Test
  void validateAndResolveCompressor_emptyClassLoader() {
    // Create a class loader that cannot load CompressorProvider services
    ClassLoader emptyClassLoader = new URLClassLoader(new URL[0], null);

    // Gzip should still work because it's hardcoded
    assertThat(CompressorUtil.validateAndResolveCompressor("gzip", emptyClassLoader))
        .isEqualTo(GzipCompressor.getInstance());

    // None should still work because it doesn't require loading services
    assertThat(CompressorUtil.validateAndResolveCompressor("none", emptyClassLoader)).isNull();

    // Any SPI-based compressor should not be available
    assertThatThrownBy(
            () -> CompressorUtil.validateAndResolveCompressor("base64", emptyClassLoader))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported compressionMethod");
  }

  @Test
  void validateAndResolveCompressor_delegatesCorrectly() {
    // Test that single-parameter method delegates to two-parameter method
    assertThat(CompressorUtil.validateAndResolveCompressor("gzip"))
        .isEqualTo(
            CompressorUtil.validateAndResolveCompressor(
                "gzip", CompressorUtil.class.getClassLoader()));

    assertThat(CompressorUtil.validateAndResolveCompressor("none"))
        .isEqualTo(
            CompressorUtil.validateAndResolveCompressor(
                "none", CompressorUtil.class.getClassLoader()));
  }
}
