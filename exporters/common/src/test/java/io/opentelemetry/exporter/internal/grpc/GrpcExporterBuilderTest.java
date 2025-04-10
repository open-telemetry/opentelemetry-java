/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.internal.ExporterMetrics;
import io.opentelemetry.exporter.internal.compression.GzipCompressor;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import java.net.URI;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrpcExporterBuilderTest {

  private GrpcExporterBuilder<Marshaler> builder;

  @BeforeEach
  void setUp() {
    builder =
        new GrpcExporterBuilder<>(
            "otlp",
            ExporterMetrics.Signal.SPAN,
            "testing",
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
    builder.setCompression(null);

    assertThat(builder).extracting("compressor").isNull();
  }

  @Test
  void compressionGzip() {
    builder.setCompression(GzipCompressor.getInstance());

    assertThat(builder).extracting("compressor").isEqualTo(GzipCompressor.getInstance());
  }

  @Test
  void compressionEnabledAndDisabled() {
    builder.setCompression(GzipCompressor.getInstance()).setCompression(null);

    assertThat(builder).extracting("compressor").isNull();
  }
}
