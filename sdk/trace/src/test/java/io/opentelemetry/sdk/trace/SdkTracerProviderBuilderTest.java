/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.SdkResourceProvider;
import io.opentelemetry.sdk.trace.internal.SdkTracerProviderUtil;
import java.util.stream.Stream;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class SdkTracerProviderBuilderTest {

  @Test
  void addResource() {
    Resource customResource =
        Resource.create(
            Attributes.of(
                AttributeKey.stringKey("custom_attribute_key"), "custom_attribute_value"));

    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder().addResource(customResource).build();

    assertThat(SdkTracerProviderUtil.getSdkResourceProvider(sdkTracerProvider).getResource())
        .isEqualTo(Resource.getDefault().merge(customResource));
  }

  @Test
  void resourceProvider_defaultsToAutoWrappedStaticResource() {
    SdkTracerProvider provider = SdkTracerProvider.builder().build();

    assertThat(SdkTracerProviderUtil.getSdkResourceProvider(provider).getResource())
        .isEqualTo(Resource.getDefault());
  }

  @Test
  void setSdkResourceProvider_usedForResource() {
    Resource customResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("key"), "value"));
    SdkResourceProvider resourceProvider = SdkResourceProvider.create(customResource);

    SdkTracerProviderBuilder builder = SdkTracerProvider.builder();
    SdkTracerProviderUtil.setSdkResourceProvider(builder, resourceProvider);
    SdkTracerProvider provider = builder.build();

    assertThat(SdkTracerProviderUtil.getSdkResourceProvider(provider)).isSameAs(resourceProvider);
    assertThat(SdkTracerProviderUtil.getSdkResourceProvider(provider).getResource())
        .isEqualTo(customResource);
  }

  @ParameterizedTest
  @MethodSource("resourceConfigurations")
  void setSdkResourceProvider_afterResource_throws(boolean addResource, Resource resource) {
    SdkTracerProviderBuilder builder = SdkTracerProvider.builder();
    if (addResource) {
      builder.addResource(resource);
    } else {
      builder.setResource(resource);
    }

    assertThatThrownBy(() -> builder.setSdkResourceProvider(SdkResourceProvider.create(resource)))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(
            "Cannot call setSdkResourceProvider after setResource or addResource has been called.");
    assertThat(SdkTracerProviderUtil.getSdkResourceProvider(builder.build()).getResource())
        .isEqualTo(addResource ? Resource.getDefault().merge(resource) : resource);
  }

  @ParameterizedTest
  @MethodSource("resourceConfigurations")
  void resource_afterSetSdkResourceProvider_throws(boolean addResource, Resource resource) {
    SdkResourceProvider resourceProvider = SdkResourceProvider.create(resource);
    SdkTracerProviderBuilder builder = SdkTracerProvider.builder();
    SdkTracerProviderUtil.setSdkResourceProvider(builder, resourceProvider);

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
    assertThat(SdkTracerProviderUtil.getSdkResourceProvider(builder.build()))
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

  @Test
  void addSpanProcessorFirst() {
    SpanProcessor firstProcessor = mock(SpanProcessor.class);
    SpanProcessor anotherProcessor = mock(SpanProcessor.class);

    SdkTracerProvider sdkTracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(anotherProcessor)
            .addSpanProcessorFirst(firstProcessor)
            .build();

    assertThat(sdkTracerProvider)
        .extracting("sharedState")
        .extracting("activeSpanProcessor")
        .extracting("spanProcessorsAll", InstanceOfAssertFactories.list(SpanProcessor.class))
        .hasSize(2)
        .containsExactly(firstProcessor, anotherProcessor);
  }
}
