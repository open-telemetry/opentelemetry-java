/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.exporter.internal.http.HttpExporterBuilder;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import org.junit.jupiter.api.Test;

class HttpExporterBuilderTest {

  private final HttpExporterBuilder<Marshaler> builder =
      new HttpExporterBuilder<>("otlp", "span", "http://localhost:4318/v1/traces");

  @Test
  void compressionDefault() {
    HttpExporter<Marshaler> exporter = builder.build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              HttpExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("httpSender")
                      .isInstanceOf(OkHttpHttpSender.class)
                      .extracting("compressionEnabled")
                      .isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionNone() {
    HttpExporter<Marshaler> exporter = builder.setCompression("none").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              HttpExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("httpSender")
                      .isInstanceOf(OkHttpHttpSender.class)
                      .extracting("compressionEnabled")
                      .isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionGzip() {
    HttpExporter<Marshaler> exporter = builder.setCompression("gzip").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              HttpExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("httpSender")
                      .isInstanceOf(OkHttpHttpSender.class)
                      .extracting("compressionEnabled")
                      .isEqualTo(true));
    } finally {
      exporter.shutdown();
    }
  }

  @Test
  void compressionEnabledAndDisabled() {
    HttpExporter<Marshaler> exporter =
        builder.setCompression("gzip").setCompression("none").build();
    try {
      assertThat(exporter)
          .isInstanceOfSatisfying(
              HttpExporter.class,
              otlp ->
                  assertThat(otlp)
                      .extracting("httpSender")
                      .isInstanceOf(OkHttpHttpSender.class)
                      .extracting("compressionEnabled")
                      .isEqualTo(false));
    } finally {
      exporter.shutdown();
    }
  }
}
