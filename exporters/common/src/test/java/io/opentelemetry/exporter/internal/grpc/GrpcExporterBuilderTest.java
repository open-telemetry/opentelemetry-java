/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.internal.compression.GzipCompressor;
import io.opentelemetry.sdk.common.export.Compressor;
import io.opentelemetry.sdk.common.internal.StandardComponentId;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrpcExporterBuilderTest {

  private GrpcExporterBuilder builder;

  @BeforeEach
  void setUp() {
    builder =
        new GrpcExporterBuilder(
            StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
            Duration.ZERO,
            URI.create("http://localhost:4317"),
            "service/method");
  }

  @Test
  void compressionDefault() {
    assertThat(builder).extracting("compressor").isNull();
  }

  @Test
  void compressionNone() {
    builder.setCompression((Compressor) null);

    assertThat(builder).extracting("compressor").isNull();
  }

  @Test
  void compressionGzip() {
    builder.setCompression(new GzipCompressor());

    assertThat(builder).extracting("compressor").isInstanceOf(GzipCompressor.class);
  }

  @Test
  void compressionEnabledAndDisabled() {
    builder.setCompression(new GzipCompressor()).setCompression((Compressor) null);

    assertThat(builder).extracting("compressor").isNull();
  }

  @Test
  void compressionString_none() {
    builder.setCompression("none");

    assertThat(builder).extracting("compressor").isNull();
  }

  @Test
  void compressionString_gzip() {
    builder.setCompression("gzip");

    assertThat(builder).extracting("compressor").isInstanceOf(GzipCompressor.class);
  }

  @Test
  void compressionString_invalid() {
    assertThatThrownBy(() -> builder.setCompression("invalid-compression"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported compressionMethod");
  }

  @Test
  void compressionString_usesServiceClassLoader() {
    // Create a class loader that cannot load Compressor services
    ComponentLoader emptyComponentLoader =
        ComponentLoader.forClassLoader(new URLClassLoader(new URL[0], null));
    builder.setComponentLoader(emptyComponentLoader);

    // This should still work because gzip compressor is hardcoded
    builder.setCompression("gzip");
    assertThat(builder).extracting("compressor").isInstanceOf(GzipCompressor.class);

    // This should still work because "none" doesn't require loading services
    builder.setCompression("none");
    assertThat(builder).extracting("compressor").isNull();
  }
}
