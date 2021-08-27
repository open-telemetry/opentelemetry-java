/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.internal.NoopMeterProvider;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OpenTelemetrySdkAutoConfigurationTest {

  @BeforeEach
  void setUp() {
    GlobalOpenTelemetry.resetForTest();
  }

  @Test
  void initializeAndGet() {
    OpenTelemetrySdk sdk = OpenTelemetrySdkAutoConfiguration.initialize();
    assertThat(GlobalOpenTelemetry.get())
        // ObfuscatedOpenTelemetry
        .extracting("delegate")
        .isSameAs(sdk);
  }

  @Test
  void initializeAndGet_noGlobal() {
    OpenTelemetrySdk sdk = OpenTelemetrySdkAutoConfiguration.initialize(false);
    // TODO: calling get() will call initialize() again and autoconfigure another instance of the
    // SDK; in that case the get() method will return OpenTelemetrySdk and not
    // ObfuscatedOpenTelemetry
    assertThat(GlobalOpenTelemetry.get()).isNotSameAs(sdk);
  }

  @Test
  void noMetricsSdk() {
    // OTEL_METRICS_EXPORTER=none so the metrics SDK should be completely disabled.
    assertThat(GlobalMeterProvider.get()).isSameAs(NoopMeterProvider.getInstance());
  }
}
