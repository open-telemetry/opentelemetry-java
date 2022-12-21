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
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporter;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class LogRecordExporterConfigurationTest {

  @Test
  void configureExporter_KnownSpiExportersOnClasspath() {
    NamedSpiManager<LogRecordExporter> spiExportersManager =
        LogRecordExporterConfiguration.logRecordExporterSpiManager(
            DefaultConfigProperties.createForTest(Collections.emptyMap()),
            LogRecordExporterConfigurationTest.class.getClassLoader());

    assertThat(LogRecordExporterConfiguration.configureExporter("logging", spiExportersManager))
        .isInstanceOf(SystemOutLogRecordExporter.class);
    assertThat(
            LogRecordExporterConfiguration.configureExporter("logging-otlp", spiExportersManager))
        .isInstanceOf(OtlpJsonLoggingLogRecordExporter.class);
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
                        DefaultConfigProperties.createForTest(
                            ImmutableMap.of("otel.exporter.otlp.protocol", "foo")),
                        LogRecordExporterConfiguration.class.getClassLoader())))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unsupported OTLP logs protocol: foo");
  }
}
