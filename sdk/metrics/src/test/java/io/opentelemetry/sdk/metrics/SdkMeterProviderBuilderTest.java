/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.SdkResourceProvider;
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

  @Test
  void resourceProvider_defaultsToAutoWrappedStaticResource() {
    SdkMeterProvider provider =
        SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create()).build();

    assertThat(SdkMeterProviderUtil.getSdkResourceProvider(provider).getResource())
        .isEqualTo(Resource.getDefault());
  }

  @Test
  void setSdkResourceProvider_usedForResource() {
    Resource customResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("key"), "value"));
    SdkResourceProvider resourceProvider = SdkResourceProvider.create(customResource);

    SdkMeterProviderBuilder builder =
        SdkMeterProvider.builder().registerMetricReader(InMemoryMetricReader.create());
    SdkMeterProviderUtil.setSdkResourceProvider(builder, resourceProvider);
    SdkMeterProvider provider = builder.build();

    assertThat(SdkMeterProviderUtil.getSdkResourceProvider(provider)).isSameAs(resourceProvider);
    assertThat(provider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("resource", customResource);
  }

  @Test
  void setSdkResourceProvider_mutuallyExclusiveWithSetResource() {
    Resource customResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("key"), "value"));
    SdkMeterProviderBuilder builder =
        SdkMeterProvider.builder()
            .registerMetricReader(InMemoryMetricReader.create())
            .setResource(customResource);
    SdkMeterProviderUtil.setSdkResourceProvider(
        builder, SdkResourceProvider.create(customResource));

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("mutually exclusive");
  }

  @Test
  void setSdkResourceProvider_mutuallyExclusiveWithAddResource() {
    Resource customResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("key"), "value"));
    SdkMeterProviderBuilder builder =
        SdkMeterProvider.builder()
            .registerMetricReader(InMemoryMetricReader.create())
            .addResource(customResource);
    SdkMeterProviderUtil.setSdkResourceProvider(
        builder, SdkResourceProvider.create(customResource));

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("mutually exclusive");
  }
}
