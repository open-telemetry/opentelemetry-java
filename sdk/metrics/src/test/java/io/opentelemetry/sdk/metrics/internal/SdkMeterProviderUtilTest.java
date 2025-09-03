/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

class SdkMeterProviderUtilTest {

  @SuppressWarnings(
      "deprecation") // Temporary deprecation to allow transition (remove after 1.55.0)
  @Test
  void setExemplarFilter() {
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    SdkMeterProviderUtil.setExemplarFilter(builder, ExemplarFilter.alwaysOn());
    assertThat(builder)
        .extracting("exemplarFilter", as(InstanceOfAssertFactories.type(ExemplarFilter.class)))
        .isEqualTo(ExemplarFilter.alwaysOn());
  }
}
