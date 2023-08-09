/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static org.assertj.core.api.Assertions.assertThat;

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
            "otlp", "span", 0, URI.create("http://localhost:4317"), null, "/test");
  }

  @Test
  void compressionDefault() {
    assertThat(builder).extracting("compressionEnabled").isEqualTo(false);
  }

  @Test
  void compressionNone() {
    builder.setCompression("none");

    assertThat(builder).extracting("compressionEnabled").isEqualTo(false);
  }

  @Test
  void compressionGzip() {
    builder.setCompression("gzip");

    assertThat(builder).extracting("compressionEnabled").isEqualTo(true);
  }

  @Test
  void compressionEnabledAndDisabled() {
    builder.setCompression("gzip").setCompression("none");

    assertThat(builder).extracting("compressionEnabled").isEqualTo(false);
  }
}
