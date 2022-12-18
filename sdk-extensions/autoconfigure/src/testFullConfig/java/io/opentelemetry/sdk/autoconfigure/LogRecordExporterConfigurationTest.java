/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import org.junit.jupiter.api.Test;

public class LogRecordExporterConfigurationTest {

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
