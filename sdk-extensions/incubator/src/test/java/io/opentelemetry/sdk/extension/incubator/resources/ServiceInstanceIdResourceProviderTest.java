/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.internal.SemConvAttributes;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ServiceInstanceIdResourceProviderTest {

  @ParameterizedTest
  @MethodSource("createResourceTestCases")
  void createResource(String expectedValue, Map<String, String> attributes) {
    ServiceInstanceIdResourceProvider provider = new ServiceInstanceIdResourceProvider();
    DefaultConfigProperties config = DefaultConfigProperties.createFromMap(Collections.emptyMap());
    AttributesBuilder builder = Attributes.builder();
    attributes.forEach(builder::put);
    Resource existing = Resource.create(builder.build());
    Resource resource =
        provider.shouldApply(config, existing) ? provider.createResource(config) : Resource.empty();

    String actual = resource.getAttributes().get(SemConvAttributes.SERVICE_INSTANCE_ID);
    if ("random".equals(expectedValue)) {
      assertThat(actual).isNotNull();
    } else {
      assertThat(actual).isEqualTo(expectedValue);
    }
  }

  static Stream<Arguments> createResourceTestCases() {
    return Stream.of(
        Arguments.argumentSet(
            "user provided service.instance.id",
            null,
            ImmutableMap.of("service.instance.id", "custom")),
        Arguments.argumentSet("random value", "random", Collections.emptyMap()));
  }
}
