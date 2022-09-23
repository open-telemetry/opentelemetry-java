/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.okhttp;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import org.junit.jupiter.api.Test;

class OkHttpExporterBuilderTest {

  private final OkHttpExporterBuilder<Marshaler> builder =
      new OkHttpExporterBuilder<>("otlp", "span", "http://localhost:4318/v1/traces");

  @Test
  void compressionDefault() {
    OkHttpExporter<Marshaler> exporter = builder.build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OkHttpExporter.class,
              otlp -> assertThat(otlp).extracting("compressionEnabled").isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionNone() {
    OkHttpExporter<Marshaler> exporter = builder.setCompression("none").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OkHttpExporter.class,
              otlp -> assertThat(otlp).extracting("compressionEnabled").isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionGzip() {
    OkHttpExporter<Marshaler> exporter = builder.setCompression("gzip").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OkHttpExporter.class,
              otlp -> assertThat(otlp).extracting("compressionEnabled").isEqualTo(true));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionEnabledAndDisabled() {
    OkHttpExporter<Marshaler> exporter =
        builder.setCompression("gzip").setCompression("none").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              OkHttpExporter.class,
              otlp -> assertThat(otlp).extracting("compressionEnabled").isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }
}
