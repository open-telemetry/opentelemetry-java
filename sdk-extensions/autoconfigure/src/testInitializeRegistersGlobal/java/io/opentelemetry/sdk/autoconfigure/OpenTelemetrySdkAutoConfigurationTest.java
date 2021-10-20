/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenTelemetrySdkAutoConfigurationTest {

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void initializeAndGet() {
    OpenTelemetrySdk sdk = AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk();
    assertThat(GlobalOpenTelemetry.get())
        // ObfuscatedOpenTelemetry
        .extracting("delegate")
        .isSameAs(sdk);
  }

  @Test
  void initializeAndGet_noGlobal() {
    OpenTelemetrySdk sdk =
        AutoConfiguredOpenTelemetrySdk.builder()
            .setResultAsGlobal(false)
            .build()
            .getOpenTelemetrySdk();
    // TODO: calling get() will call initialize() again and autoconfigure another instance of the
    // SDK; in that case the get() method will return OpenTelemetrySdk and not
    // ObfuscatedOpenTelemetry
    assertThat(GlobalOpenTelemetry.get()).isNotSameAs(sdk);
  }

  @Test
  void noMetricsSdk() {
    // OTEL_METRICS_EXPORTER=none so the metrics SDK should be completely disabled.
    // This is a bit of an odd test, so we just ensure that we don't have the same impl class as if
    // we instantiated an SDK with a reader.
    assertThat(GlobalMeterProvider.get())
        .doesNotHaveSameClassAs(
            SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build());
  }
}
