/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.internal.testing.CleanupExtension;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.CardinalityLimitSelector;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MeterProviderConfigurationTest {

  @RegisterExtension CleanupExtension cleanup = new CleanupExtension();

  private final SpiHelper spiHelper =
      SpiHelper.create(MeterProviderConfigurationTest.class.getClassLoader());

  @Test
  void configureMeterProvider_InvalidCardinalityLimit() {
    List<Closeable> closeables = new ArrayList<>();

    assertThatThrownBy(
            () -> {
              MeterProviderConfiguration.configureMeterProvider(
                  SdkMeterProvider.builder(),
                  DefaultConfigProperties.createFromMap(
                      ImmutableMap.of(
                          "otel.metrics.exporter",
                          "logging",
                          "otel.java.metrics.cardinality.limit",
                          "0")),
                  spiHelper,
                  (a, b) -> a,
                  (a, b) -> a,
                  closeables);
            })
        .isInstanceOf(ConfigurationException.class)
        .hasMessage("otel.java.metrics.cardinality.limit must be >= 1");
    cleanup.addCloseables(closeables);
  }

  @Test
  void configureMeterProvider_ConfiguresCardinalityLimit() {
    List<Closeable> closeables = new ArrayList<>();

    // Verify default cardinality
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    MeterProviderConfiguration.configureMeterProvider(
        builder,
        DefaultConfigProperties.createFromMap(
            Collections.singletonMap("otel.metrics.exporter", "logging")),
        spiHelper,
        (a, b) -> a,
        (a, b) -> a,
        closeables);
    cleanup.addCloseables(closeables);
    assertCardinalityLimit(builder, 2000);

    // Customized limit cardinality limit to 100
    builder = SdkMeterProvider.builder();
    MeterProviderConfiguration.configureMeterProvider(
        builder,
        DefaultConfigProperties.createFromMap(
            ImmutableMap.of(
                "otel.metrics.exporter",
                "logging",
                "otel.java.metrics.cardinality.limit",
                "100",
                // otel.java.metrics.cardinality.limit takes priority over deprecated property
                "otel.experimental.metrics.cardinality.limit",
                "200")),
        spiHelper,
        (a, b) -> a,
        (a, b) -> a,
        closeables);
    cleanup.addCloseables(closeables);
    assertCardinalityLimit(builder, 100);

    // Deprecated property
    builder = SdkMeterProvider.builder();
    MeterProviderConfiguration.configureMeterProvider(
        builder,
        DefaultConfigProperties.createFromMap(
            ImmutableMap.of(
                "otel.metrics.exporter",
                "logging",
                "otel.experimental.metrics.cardinality.limit",
                "100")),
        spiHelper,
        (a, b) -> a,
        (a, b) -> a,
        closeables);
    cleanup.addCloseables(closeables);
    assertCardinalityLimit(builder, 100);
  }

  private static void assertCardinalityLimit(
      SdkMeterProviderBuilder builder, int expectedCardinalityLimit) {
    assertThat(builder)
        .extracting(
            "metricReaders",
            as(InstanceOfAssertFactories.map(MetricReader.class, CardinalityLimitSelector.class)))
        .hasSizeGreaterThan(0)
        .allSatisfy(
            (metricReader, cardinalityLimitSelector) -> {
              for (InstrumentType instrumentType : InstrumentType.values()) {
                assertThat(cardinalityLimitSelector.getCardinalityLimit(instrumentType))
                    .isEqualTo(expectedCardinalityLimit);
              }
            });
  }
}
