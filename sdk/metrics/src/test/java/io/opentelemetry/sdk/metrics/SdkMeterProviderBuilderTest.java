/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.InstanceOfAssertFactories;
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

  @Test
  void setMinimumCollectionInterval() {
    assertThat(SdkMeterProvider.builder().setMinimumCollectionInterval(Duration.ofSeconds(10)))
        .extracting(
            "minimumCollectionIntervalNanos", as(InstanceOfAssertFactories.type(Long.class)))
        .isEqualTo(TimeUnit.SECONDS.toNanos(10));

    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    SdkMeterProviderUtil.setMinimumCollectionInterval(builder, Duration.ofSeconds(10));
    assertThat(builder)
        .extracting(
            "minimumCollectionIntervalNanos", as(InstanceOfAssertFactories.type(Long.class)))
        .isEqualTo(TimeUnit.SECONDS.toNanos(10));
  }
}
