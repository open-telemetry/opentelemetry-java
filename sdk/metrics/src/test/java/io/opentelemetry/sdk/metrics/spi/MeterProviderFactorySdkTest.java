/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.spi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.metrics.GlobalMetricsProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MeterProviderFactorySdk}. */
class MeterProviderFactorySdkTest {
  @Test
  void testDefault() {
    assertThat(GlobalMetricsProvider.get()).isInstanceOf(SdkMeterProvider.class);
  }
}
