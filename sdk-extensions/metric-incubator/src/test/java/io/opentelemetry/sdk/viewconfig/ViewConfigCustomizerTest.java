/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.viewconfig;

import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ViewConfigCustomizerTest {

  @Test
  void customizeMeterProvider_Spi() {
    System.setProperty("otel.traces.exporter", "none");
    System.setProperty(
        "otel.experimental.metric.view.config",
        ViewConfigTest.class.getResource("/view-config-customizer-test.yaml").getFile());

    InMemoryMetricReader reader = InMemoryMetricReader.create();
    AutoConfiguredOpenTelemetrySdk.builder()
        .setResultAsGlobal(false)
        .addMeterProviderCustomizer(
            (meterProviderBuilder, configProperties) ->
                meterProviderBuilder.registerMetricReader(reader))
        .build()
        .getOpenTelemetrySdk()
        .getSdkMeterProvider()
        .get("test-meter")
        .counterBuilder("counter")
        .buildWithCallback(
            callback -> {
              // Attributes with keys baz and qux should be filtered out
              callback.record(
                  1,
                  Attributes.builder()
                      .put("foo", "val")
                      .put("bar", "val")
                      .put("baz", "val")
                      .put("qux", "val")
                      .build());
            });

    assertThat(reader.collectAllMetrics())
        .hasSize(1)
        .satisfiesExactly(
            metricData -> {
              assertThat(metricData)
                  .hasLongSum()
                  .points()
                  .satisfiesExactly(
                      point ->
                          assertThat(point)
                              .hasValue(1)
                              .hasAttributes(
                                  Attributes.builder()
                                      .put("foo", "val")
                                      .put("bar", "val")
                                      .build()));
            });
  }

  @Test
  void customizeMeterProvider_MultipleFiles() {
    ConfigProperties properties =
        withConfigFileLocations(
            Arrays.asList(
                ViewConfigTest.class.getResource("/view-config-customizer-test.yaml").getFile(),
                ViewConfigTest.class.getResource("/full-config.yaml").getFile()));

    assertThatCode(
            () ->
                ViewConfigCustomizer.customizeMeterProvider(SdkMeterProvider.builder(), properties))
        .doesNotThrowAnyException();
  }

  @Test
  void customizeMeterProvider_InvalidFile() {
    ConfigProperties properties =
        withConfigFileLocations(
            Arrays.asList(
                ViewConfigTest.class.getResource("/empty-selector-config.yaml").getFile()));

    assertThatThrownBy(
            () ->
                ViewConfigCustomizer.customizeMeterProvider(SdkMeterProvider.builder(), properties))
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("Invalid view configuration - empty selector specification.");
  }

  private static ConfigProperties withConfigFileLocations(List<String> fileLocations) {
    ConfigProperties properties = mock(ConfigProperties.class);
    when(properties.getList("otel.experimental.metric.view.config")).thenReturn(fileLocations);
    return properties;
  }
}
