/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.sdk.resources.Resource;
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

  @Test
  void defaultResource() {
    SdkMeterProvider meterProvider = SdkMeterProvider.builder().build();

    assertThat(meterProvider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("resource", Resource.getDefault());
  }
}
