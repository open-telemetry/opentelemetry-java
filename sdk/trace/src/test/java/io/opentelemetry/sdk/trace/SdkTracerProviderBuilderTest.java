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
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;

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

  @Test
  void setSdkResourceProvider_mutuallyExclusiveWithSetResource() {
    Resource customResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("key"), "value"));
    SdkTracerProviderBuilder builder = SdkTracerProvider.builder().setResource(customResource);
    SdkTracerProviderUtil.setSdkResourceProvider(
        builder, SdkResourceProvider.create(customResource));

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("mutually exclusive");
  }

  @Test
  void setSdkResourceProvider_mutuallyExclusiveWithAddResource() {
    Resource customResource =
        Resource.create(Attributes.of(AttributeKey.stringKey("key"), "value"));
    SdkTracerProviderBuilder builder = SdkTracerProvider.builder().addResource(customResource);
    SdkTracerProviderUtil.setSdkResourceProvider(
        builder, SdkResourceProvider.create(customResource));

    assertThatThrownBy(builder::build)
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("mutually exclusive");
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
