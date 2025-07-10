/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.compression.GzipCompressor;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.internal.StandardComponentId;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrpcExporterBuilderTest {

  private GrpcExporterBuilder<Marshaler> builder;

  @BeforeEach
  void setUp() {
    builder =
        new GrpcExporterBuilder<>(
            StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER,
            0,
            URI.create("http://localhost:4317"),
            null,
            "/test");
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
    builder.setCompression(GzipCompressor.getInstance());

    assertThat(builder).extracting("compressor").isEqualTo(GzipCompressor.getInstance());
  }

  @Test
  void compressionEnabledAndDisabled() {
    builder.setCompression(GzipCompressor.getInstance()).setCompression((Compressor) null);

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

    assertThat(builder).extracting("compressor").isEqualTo(GzipCompressor.getInstance());
  }

  @Test
  void compressionString_invalid() {
    assertThatThrownBy(() -> builder.setCompression("invalid-compression"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("Unsupported compressionMethod");
  }

  @Test
  void compressionString_usesServiceClassLoader() {
    // Create a class loader that cannot load CompressorProvider services
    ComponentLoader emptyComponentLoader =
        ComponentLoader.forClassLoader(new URLClassLoader(new URL[0], null));
    builder.setComponentLoader(emptyComponentLoader);

    // This should still work because gzip compressor is hardcoded
    builder.setCompression("gzip");
    assertThat(builder).extracting("compressor").isEqualTo(GzipCompressor.getInstance());

    // This should still work because "none" doesn't require loading services
    builder.setCompression("none");
    assertThat(builder).extracting("compressor").isNull();
  }
}
