/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.assertj.core.api.MapAssert;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class HostIdResourceProviderTest {

  private static class TestCase {
    private final String name;
    private final String expectedValue;
    private final Function<Path, List<String>> pathReader;

    private TestCase(String name, String expectedValue, Function<Path, List<String>> pathReader) {
      this.name = name;
      this.expectedValue = expectedValue;
      this.pathReader = pathReader;
    }
  }

  private static final List<TestCase> TEST_CASES =
      Arrays.asList(
          new TestCase("default", "test", path -> Collections.singletonList("test")),
          new TestCase("empty file", null, path -> Collections.emptyList()),
          new TestCase(
              "error reading",
              null,
              path -> {
                throw new IllegalStateException("can't read file");
              }));

  @TestFactory
  Collection<DynamicTest> createResource() {
    return TEST_CASES.stream()
        .map(
            testCase ->
                DynamicTest.dynamicTest(
                    testCase.name,
                    () -> {
                      HostIdResourceProvider provider =
                          new HostIdResourceProvider(testCase.pathReader);
                      MapAssert<AttributeKey<?>, Object> that =
                          assertThat(provider.createResource(null).getAttributes().asMap());

                      if (testCase.expectedValue == null) {
                        that.isEmpty();
                      } else {
                        that.containsEntry(HostIdResourceProvider.HOST_ID, testCase.expectedValue);
                      }
                    }))
        .collect(Collectors.toList());
  }

  @Test
  void shouldApply() {
    HostIdResourceProvider provider = new HostIdResourceProvider();
    assertThat(
            provider.shouldApply(
                DefaultConfigProperties.createFromMap(Collections.emptyMap()), null))
        .isTrue();
    assertThat(
            provider.shouldApply(
                DefaultConfigProperties.createFromMap(
                    Collections.singletonMap("otel.resource.attributes", "host.id=foo")),
                null))
        .isFalse();
  }
}
