/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.exporter.logging.SystemOutLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.logs.OtlpStdoutLogRecordExporter;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.sdk.autoconfigure.internal.NamedSpiManager;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class LogRecordExporterConfigurationTest {

  private final SpiHelper spiHelper =
      SpiHelper.create(LogRecordExporterConfigurationTest.class.getClassLoader());

  @Test
  void configureExporter_KnownSpiExportersOnClasspath() {
    NamedSpiManager<LogRecordExporter> spiExportersManager =
        LogRecordExporterConfiguration.logRecordExporterSpiManager(
            DefaultConfigProperties.createFromMap(Collections.emptyMap()), spiHelper);

    assertThat(LogRecordExporterConfiguration.configureExporter("console", spiExportersManager))
        .isInstanceOf(SystemOutLogRecordExporter.class);
    assertThat(LogRecordExporterConfiguration.configureExporter("logging", spiExportersManager))
        .isInstanceOf(SystemOutLogRecordExporter.class);
    assertThat(
            LogRecordExporterConfiguration.configureExporter("logging-otlp", spiExportersManager))
        .isInstanceOf(OtlpJsonLoggingLogRecordExporter.class);
    assertThat(LogRecordExporterConfiguration.configureExporter("experimental-otlp/stdout", spiExportersManager))
        .isInstanceOf(OtlpStdoutLogRecordExporter.class);
    assertThat(LogRecordExporterConfiguration.configureExporter("otlp", spiExportersManager))
        .isInstanceOf(OtlpGrpcLogRecordExporter.class);
  }

  @Test
  void configureExporter_UnsupportedOtlpProtocol() {
    assertThatThrownBy(
            () ->
                LogRecordExporterConfiguration.configureExporter(
                    "otlp",
                    LogRecordExporterConfiguration.logRecordExporterSpiManager(
                        DefaultConfigProperties.createFromMap(
                            ImmutableMap.of("otel.exporter.otlp.protocol", "foo")),
                        spiHelper)))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP logs protocol: foo");
  }
}
