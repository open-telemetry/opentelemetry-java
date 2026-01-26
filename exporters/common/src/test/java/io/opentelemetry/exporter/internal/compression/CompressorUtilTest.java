/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.compression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.common.ComponentLoader;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.api.Test;

class CompressorUtilTest {

  private final ComponentLoader componentLoader =
      ComponentLoader.forClassLoader(CompressorUtilTest.class.getClassLoader());

  @Test
  void validateAndResolveCompressor_none() {
    assertThat(CompressorUtil.validateAndResolveCompressor("none")).isNull();
  }

  @Test
  void validateAndResolveCompressor_gzip() {
    assertThat(CompressorUtil.validateAndResolveCompressor("gzip"))
        .isInstanceOf(GzipCompressor.class);
  }

  @Test
  void validateAndResolveCompressor_invalid() {
    assertThatThrownBy(() -> CompressorUtil.validateAndResolveCompressor("invalid"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported compressionMethod");
  }

  @Test
  void validateAndResolveCompressor_withClassLoader_none() {
    assertThat(CompressorUtil.validateAndResolveCompressor("none", componentLoader)).isNull();
  }

  @Test
  void validateAndResolveCompressor_withClassLoader_gzip() {
    assertThat(CompressorUtil.validateAndResolveCompressor("gzip", componentLoader))
        .isInstanceOf(GzipCompressor.class);
  }

  @Test
  void validateAndResolveCompressor_withClassLoader_invalid() {
    assertThatThrownBy(
            () -> CompressorUtil.validateAndResolveCompressor("invalid", componentLoader))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported compressionMethod");
  }

  @Test
  void validateAndResolveCompressor_emptyClassLoader() {
    // Create a class loader that cannot load Compressor services
    ComponentLoader emptyComponentLoader =
        ComponentLoader.forClassLoader(new URLClassLoader(new URL[0], null));

    // Gzip should still work because it's hardcoded
    assertThat(CompressorUtil.validateAndResolveCompressor("gzip", emptyComponentLoader))
        .isInstanceOf(GzipCompressor.class);

    // None should still work because it doesn't require loading services
    assertThat(CompressorUtil.validateAndResolveCompressor("none", emptyComponentLoader)).isNull();

    // Any SPI-based compressor should not be available
    assertThatThrownBy(
            () -> CompressorUtil.validateAndResolveCompressor("base64", emptyComponentLoader))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported compressionMethod");
  }

  @Test
  void validateAndResolveCompressor_delegatesCorrectly() {
    // Test that single-parameter method delegates to two-parameter method
    assertThat(CompressorUtil.validateAndResolveCompressor("gzip"))
        .isEqualTo(CompressorUtil.validateAndResolveCompressor("gzip", componentLoader));

    assertThat(CompressorUtil.validateAndResolveCompressor("none"))
        .isEqualTo(CompressorUtil.validateAndResolveCompressor("none", componentLoader));
  }
}
