/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.metric.viewconfig;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.util.Arrays;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ViewConfigCustomizerTest {

  @Test
  void customizeMeterProvider_Spi() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    AutoConfiguredOpenTelemetrySdk.builder()
        .setResultAsGlobal(false)
        .addPropertiesSupplier(
            () ->
                ImmutableMap.of(
                    "otel.traces.exporter",
                    "none",
                    "otel.metrics.exporter",
                    "none",
                    "otel.experimental.metrics.view.config",
                    "classpath:/view-config-customizer-test.yaml"))
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
        .satisfiesExactly(
            metricData ->
                assertThat(metricData)
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1)
                                        .hasAttributes(
                                            attributeEntry("foo", "val"),
                                            attributeEntry("bar", "val")))));
  }

  @Test
  void customizeMeterProvider_MultipleFiles() {
    ConfigProperties properties =
        withConfigFileLocations(
            "classpath:/view-config-customizer-test.yaml", "classpath:/full-config.yaml");

    assertThatCode(
            () ->
                ViewConfigCustomizer.customizeMeterProvider(SdkMeterProvider.builder(), properties))
        .doesNotThrowAnyException();
  }

  @Test
  void customizeMeterProvider_AbsolutePath() {
    ConfigProperties properties =
        withConfigFileLocations(
            ViewConfigTest.class.getResource("/view-config-customizer-test.yaml").getFile());

    assertThatCode(
            () ->
                ViewConfigCustomizer.customizeMeterProvider(SdkMeterProvider.builder(), properties))
        .doesNotThrowAnyException();
  }

  @Test
  void customizeMeterProvider_Invalid() {
    assertThatThrownBy(
            () ->
                ViewConfigCustomizer.customizeMeterProvider(
                    SdkMeterProvider.builder(),
                    withConfigFileLocations("classpath:" + UUID.randomUUID())))
        .hasMessageMatching("Resource .* not found on classpath of classloader .*");
    assertThatThrownBy(
            () ->
                ViewConfigCustomizer.customizeMeterProvider(
                    SdkMeterProvider.builder(), withConfigFileLocations("/" + UUID.randomUUID())))
        .hasMessageContaining("View config file not found:");
    assertThatThrownBy(
            () ->
                ViewConfigCustomizer.customizeMeterProvider(
                    SdkMeterProvider.builder(),
                    withConfigFileLocations("classpath:/empty-selector-config.yaml")))
        .hasMessageContaining("Failed to parse view config")
        .hasRootCauseMessage("selector is required");
  }

  private static ConfigProperties withConfigFileLocations(String... fileLocations) {
    ConfigProperties properties = mock(ConfigProperties.class);
    when(properties.getList("otel.experimental.metrics.view.config"))
        .thenReturn(Arrays.asList(fileLocations));
    return properties;
  }
}
