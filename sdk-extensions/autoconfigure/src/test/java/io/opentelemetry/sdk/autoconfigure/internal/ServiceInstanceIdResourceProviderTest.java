/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class ServiceInstanceIdResourceProviderTest {

  private static class TestCase {
    private final String name;
    final String expectedValue;
    final Map<String, String> attributes;

    TestCase(String name, String expectedValue, Map<String, String> attributes) {
      this.name = name;
      this.expectedValue = expectedValue;
      this.attributes = attributes;
    }
  }

  @TestFactory
  Stream<DynamicTest> createResource() {
    return Stream.of(
            new TestCase(
                "user provided service.instance.id",
                null,
                ImmutableMap.of("service.instance.id", "custom")),
            new TestCase("random value", "random", Collections.emptyMap()))
        .map(
            testCase ->
                DynamicTest.dynamicTest(
                    testCase.name,
                    () -> {
                      ServiceInstanceIdResourceProvider provider =
                          new ServiceInstanceIdResourceProvider();
                      DefaultConfigProperties config =
                          DefaultConfigProperties.createFromMap(Collections.emptyMap());
                      AttributesBuilder builder = Attributes.builder();
                      testCase.attributes.forEach(builder::put);
                      Resource existing = Resource.create(builder.build());
                      Resource resource =
                          provider.shouldApply(config, existing)
                              ? provider.createResource(config)
                              : Resource.empty();

                      String actual =
                          resource
                              .getAttributes()
                              .get(ServiceInstanceIdResourceProvider.SERVICE_INSTANCE_ID);
                      if ("random".equals(testCase.expectedValue)) {
                        assertThat(actual).isNotNull();
                      } else {
                        assertThat(actual).isEqualTo(testCase.expectedValue);
                      }
                    }));
  }
}
