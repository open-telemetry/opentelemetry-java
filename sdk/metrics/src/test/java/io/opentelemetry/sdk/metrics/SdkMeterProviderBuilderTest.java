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
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

  @ParameterizedTest
  @MethodSource("resourceConfigurations")
  void setSdkResourceProvider_afterResource_throws(boolean addResource, Resource resource) {
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    if (addResource) {
      builder.addResource(resource);
    } else {
      builder.setResource(resource);
    }

    assertThatThrownBy(() -> builder.setSdkResourceProvider(SdkResourceProvider.create(resource)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Cannot call setSdkResourceProvider after setResource or addResource has been called.");
    assertThat(SdkMeterProviderUtil.getSdkResourceProvider(builder.build()).getResource())
        .isEqualTo(addResource ? Resource.getDefault().merge(resource) : resource);
  }

  @ParameterizedTest
  @MethodSource("resourceConfigurations")
  void resource_afterSetSdkResourceProvider_throws(boolean addResource, Resource resource) {
    SdkResourceProvider resourceProvider = SdkResourceProvider.create(resource);
    SdkMeterProviderBuilder builder = SdkMeterProvider.builder();
    SdkMeterProviderUtil.setSdkResourceProvider(builder, resourceProvider);

    assertThatThrownBy(
            () -> {
              if (addResource) {
                builder.addResource(resource);
              } else {
                builder.setResource(resource);
              }
            })
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Cannot call "
                + (addResource ? "addResource" : "setResource")
                + " after setSdkResourceProvider has been called.");
    assertThat(SdkMeterProviderUtil.getSdkResourceProvider(builder.build()))
        .isSameAs(resourceProvider);
  }

  private static Stream<Arguments> resourceConfigurations() {
    Resource customResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("key"), "value"));
    return Stream.of(
        Arguments.argumentSet("setResource(custom)", false, customResource),
        Arguments.argumentSet("setResource(default)", false, Resource.getDefault()),
        Arguments.argumentSet("setResource(empty)", false, Resource.empty()),
        Arguments.argumentSet("addResource(custom)", true, customResource),
        Arguments.argumentSet("addResource(default)", true, Resource.getDefault()),
        Arguments.argumentSet("addResource(empty)", true, Resource.empty()));
  }
}
