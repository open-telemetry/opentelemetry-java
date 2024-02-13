/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

class ServiceInstanceIdResourceProviderTest {

  private static class TestCase {
    private final String name;
    final String expectedValue;
    final String expectedVariant;
    final Map<String, String> attributes;

    TestCase(
        String name, String expectedValue, String expectedVariant, Map<String, String> attributes) {
      this.name = name;
      this.expectedValue = expectedValue;
      this.expectedVariant = expectedVariant;
      this.attributes = attributes;
    }
  }

  private static final List<TestCase> TEST_CASES =
      Arrays.asList(
          new TestCase(
              "user provided service.instance.id",
              null,
              "none",
              ImmutableMap.of(
                  "service.instance.id",
                  "custom",
                  "service.name",
                  "service",
                  "service.namespace",
                  "ns",
                  "host.id",
                  "host")),
          new TestCase(
              "user requested random service.instance.id",
              "random",
              "uuidv4",
              ImmutableMap.of(
                  "service.instance.id",
                  "uuidv4",
                  "service.name",
                  "service",
                  "service.namespace",
                  "ns",
                  "host.id",
                  "host")),
          new TestCase(
              "k8s pod",
              "8ded023e-2034-5633-a0c2-f86ab54f8909",
              "telemetry.sdk.name,telemetry.sdk.language,k8s.namespace.name,k8s.pod.name,k8s.container.name",
              ImmutableMap.of(
                  "telemetry.sdk.name",
                  "opentelemetry",
                  "telemetry.sdk.language",
                  "go",
                  "k8s.pod.name",
                  "vendors-pqr-jh7d2",
                  "k8s.namespace.name",
                  "accounting",
                  "k8s.container.name",
                  "some-sidecar",
                  "service.name",
                  "customers",
                  "host.id",
                  "graviola")),
          new TestCase(
              "host with service namespace",
              "b6c5414c-1aae-5e72-aeea-866f47b7ea64",
              "telemetry.sdk.name,telemetry.sdk.language,service.namespace,service.name,host.id",
              ImmutableMap.of(
                  "telemetry.sdk.name",
                  "opentelemetry",
                  "telemetry.sdk.language",
                  "go",
                  "service.name",
                  "service",
                  "service.namespace",
                  "ns",
                  "host.id",
                  "host")),
          new TestCase(
              "host without service namespace",
              "17ffc8fd-6ed7-5069-a5fb-2fed78f5455f",
              "telemetry.sdk.name,telemetry.sdk.language,service.name,host.id",
              ImmutableMap.of(
                  "telemetry.sdk.name",
                  "opentelemetry",
                  "telemetry.sdk.language",
                  "go",
                  "service.name",
                  "customers",
                  "host.id",
                  "graviola")),
          new TestCase(
              "random value - default service name",
              "random",
              null,
              ImmutableMap.of(
                  "telemetry.sdk.name",
                  "opentelemetry",
                  "telemetry.sdk.language",
                  "go",
                  "service.name",
                  "unknown_service:java",
                  "service.namespace",
                  "ns",
                  "host.id",
                  "host")),
          new TestCase(
              "random value - no host found",
              "random",
              null,
              ImmutableMap.of(
                  "telemetry.sdk.name",
                  "opentelemetry",
                  "telemetry.sdk.language",
                  "go",
                  "service.name",
                  "service",
                  "service.namespace",
                  "ns")));

  @TestFactory
  Collection<DynamicTest> createResource() {
    return TEST_CASES.stream()
        .map(
            testCase ->
                DynamicTest.dynamicTest(testCase.name, () -> runCreateResourceTest(testCase)))
        .collect(Collectors.toList());
  }

  private static void runCreateResourceTest(TestCase testCase) {
    // use "go" to make it comparable to the spec
    // https://github.com/open-telemetry/semantic-conventions/pull/312
    Map<String, String> map = new HashMap<>(testCase.attributes);
    map.put("telemetry.sdk.name", "opentelemetry");
    map.put("telemetry.sdk.language", "go");
    Attributes attributes = parseAttributes(map);

    Optional<ServiceInstanceIdResourceProvider.Variant> variant =
        ServiceInstanceIdResourceProvider.findVariant(attributes);
    assertThat(variant.map(ServiceInstanceIdResourceProvider.Variant::toString).orElse(null))
        .isEqualTo(testCase.expectedVariant);

    Resource resource = ServiceInstanceIdResourceProvider.createResource(attributes);

    String actual =
        resource.getAttributes().get(ServiceInstanceIdResourceProvider.SERVICE_INSTANCE_ID);
    if ("random".equals(testCase.expectedValue)) {
      assertThat(actual).isNotNull();
    } else {
      assertThat(actual).isEqualTo(testCase.expectedValue);
    }
  }

  private static Attributes parseAttributes(Map<String, String> map) {
    AttributesBuilder builder = Attributes.builder();
    for (Map.Entry<String, String> entry : map.entrySet()) {
      builder.put(entry.getKey(), entry.getValue());
    }

    return builder.build();
  }
}
