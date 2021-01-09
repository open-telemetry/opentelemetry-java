/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import org.junit.jupiter.api.Test;

class SdkMeterProviderBuilderTest {

  @Test
  void buildAndRegisterGlobal() {
    SdkMeterProvider meterProvider = SdkMeterProvider.builder().buildAndRegisterGlobal();
    try {
      assertThat(GlobalMetricsProvider.get()).isSameAs(meterProvider);
    } finally {
      GlobalMetricsProvider.set(null);
    }
  }
}
