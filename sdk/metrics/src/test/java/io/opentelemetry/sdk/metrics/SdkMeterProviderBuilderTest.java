/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import io.opentelemetry.sdk.resources.Resource;
import org.junit.jupiter.api.Test;

class SdkMeterProviderBuilderTest {

  @Test
  void defaultResource() {
    // We need a reader to have a resource.
    SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build();

    assertThat(meterProvider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("resource", Resource.getDefault());
  }
}
