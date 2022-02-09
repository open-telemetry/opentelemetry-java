/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static io.opentelemetry.sdk.autoconfigure.OtlpConfigUtil.PROTOCOL_HTTP_PROTOBUF;
import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.function.BiFunction;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

public class ConfigurableMetricExporterTest {

  @Test
  void configuration() {
    ConfigProperties config =
        DefaultConfigProperties.createForTest(ImmutableMap.of("test.option", "true"));
    MetricExporter metricExporter =
        MetricExporterConfiguration.configureSpiExporter(
            "testExporter", config, MetricExporterConfiguration.class.getClassLoader());

    assertThat(metricExporter)
        .isInstanceOf(TestConfigurableMetricExporterProvider.TestMetricExporter.class)
        .extracting("config")
        .isSameAs(config);
  }

  @Test
  void emptyClassLoader() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "testExporter",
                    DefaultConfigProperties.createForTest(Collections.emptyMap()),
                    new URLClassLoader(new URL[0], null),
                    SdkMeterProvider.builder(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("testExporter");
  }

  @Test
  void exporterNotFound() {
    assertThatThrownBy(
            () ->
                MetricExporterConfiguration.configureExporter(
                    "catExporter",
                    DefaultConfigProperties.createForTest(Collections.emptyMap()),
                    MetricExporterConfiguration.class.getClassLoader(),
                    SdkMeterProvider.builder(),
                    (a, unused) -> a))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("catExporter");
  }

  @Test
  void configureExporter_OtlpHttpExporterNotOnClasspath() {
    // Use the OTLP http/protobuf exporter which is not on the classpath
    ConfigProperties configProperties =
        DefaultConfigProperties.createForTest(
            ImmutableMap.of("otel.exporter.otlp.protocol", PROTOCOL_HTTP_PROTOBUF));
    SdkMeterProviderBuilder meterProviderBuilder = SdkMeterProvider.builder();
    BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
        metricCustomizer =
            spy(
                new BiFunction<MetricExporter, ConfigProperties, MetricExporter>() {
                  @Override
                  public MetricExporter apply(
                      MetricExporter metricExporter, ConfigProperties configProperties) {
                    return metricExporter;
                  }
                });

    MetricExporterConfiguration.configureExporter(
        "otlp",
        configProperties,
        MetricExporterConfiguration.class.getClassLoader(),
        meterProviderBuilder,
        metricCustomizer);

    // Should not call customizer or register a metric reader
    verify(metricCustomizer, never()).apply(any(), any());
    assertThat(meterProviderBuilder)
        .extracting("metricReaders", as(InstanceOfAssertFactories.list(MetricReaderFactory.class)))
        .hasSize(0);
  }
}
