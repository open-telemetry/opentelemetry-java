/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.resources.Resource;
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

    assertThat(sdkTracerProvider)
        .extracting("sharedState")
        .hasFieldOrPropertyWithValue("resource", Resource.getDefault().merge(customResource));
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
