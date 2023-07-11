/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
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
  void addResource() {
    Resource customResource =
        Resource.create(
            Attributes.of(
                AttributeKey.stringKey("custom_attribute_key"), "custom_attribute_value"));

    SdkMeterProvider sdkMeterProvider =
        SdkMeterProvider.builder()
            .registerMetricReader(InMemoryMetricReader.create())
            .addResource(customResource)
            .build();

    assertThat(sdkMeterProvider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("resource", Resource.getDefault().merge(customResource));
  }
}
