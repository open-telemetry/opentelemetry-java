/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporterProvider;
import io.opentelemetry.exporter.logging.otlp.internal.metrics.OtlpStdoutMetricExporterProvider;
import io.opentelemetry.exporter.logging.otlp.internal.trace.OtlpStdoutSpanExporterProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.util.Collections;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class OtlpStdoutExporterProviderTest {

  @Test
  void logRecordExporterProvider() {
    OtlpStdoutLogRecordExporterProvider provider = new OtlpStdoutLogRecordExporterProvider();
    Assertions.assertThat(provider.getName()).isEqualTo("otlp-stdout");
    Assertions.assertThat(
            provider.createExporter(DefaultConfigProperties.createFromMap(Collections.emptyMap())))
        .isInstanceOf(OtlpJsonLoggingLogRecordExporter.class);
  }

  @Test
  void metricExporterProvider() {
    OtlpStdoutMetricExporterProvider provider = new OtlpStdoutMetricExporterProvider();
    assertThat(provider.getName()).isEqualTo("otlp-stdout");
    assertThat(
            provider.createExporter(DefaultConfigProperties.createFromMap(Collections.emptyMap())))
        .isInstanceOf(OtlpJsonLoggingMetricExporter.class);
    // todo metric exporter does not have any configuration
  }

  @Test
  void spanExporterProvider() {
    OtlpStdoutSpanExporterProvider provider = new OtlpStdoutSpanExporterProvider();
    assertThat(provider.getName()).isEqualTo("otlp-stdout");
    assertThat(
            provider.createExporter(DefaultConfigProperties.createFromMap(Collections.emptyMap())))
        .isInstanceOf(OtlpJsonLoggingSpanExporter.class);
  }
}
