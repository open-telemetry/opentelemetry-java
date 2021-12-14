/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricExporter;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class SdkMeterProviderBuilderTest {

  @Test
  @SuppressWarnings("deprecation") // Testing deprecated methods
  void buildAndRegisterGlobal() {
    SdkMeterProvider meterProvider = SdkMeterProvider.builder().buildAndRegisterGlobal();
    try {
      assertThat(io.opentelemetry.api.metrics.GlobalMeterProvider.get()).isSameAs(meterProvider);
    } finally {
      io.opentelemetry.api.metrics.GlobalMeterProvider.set(null);
    }
  }

  @Test
  void defaultResource() {
    // We need a reader to have a resource.
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(
                PeriodicMetricReader.newMetricReaderFactory(InMemoryMetricExporter.create()))
            .build();

    assertThat(meterProvider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("resource", Resource.getDefault());
  }
}
