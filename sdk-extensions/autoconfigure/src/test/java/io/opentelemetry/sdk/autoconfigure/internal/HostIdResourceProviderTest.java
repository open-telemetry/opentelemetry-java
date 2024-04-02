/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.assertj.core.api.MapAssert;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

class HostIdResourceProviderTest {

  private static class LinuxTestCase {
    private final String name;
    private final String expectedValue;
    private final Function<Path, List<String>> pathReader;

    private LinuxTestCase(
        String name, String expectedValue, Function<Path, List<String>> pathReader) {
      this.name = name;
      this.expectedValue = expectedValue;
      this.pathReader = pathReader;
    }
  }

  private static class WindowsTestCase {
    private final String name;
    private final String expectedValue;
    private final Supplier<HostIdResourceProvider.ExecResult> queryWindowsRegistry;

    private WindowsTestCase(
        String name,
        String expectedValue,
        Supplier<HostIdResourceProvider.ExecResult> queryWindowsRegistry) {
      this.name = name;
      this.expectedValue = expectedValue;
      this.queryWindowsRegistry = queryWindowsRegistry;
    }
  }

  @TestFactory
  Collection<DynamicTest> createResourceLinux() {
    return Stream.of(
            new LinuxTestCase("default", "test", path -> Collections.singletonList("test")),
            new LinuxTestCase("empty file", null, path -> Collections.emptyList()),
            new LinuxTestCase(
                "error reading",
                null,
                path -> {
                  throw new IllegalStateException("can't read file");
                }))
        .map(
            testCase ->
                DynamicTest.dynamicTest(
                    testCase.name,
                    () -> {
                      HostIdResourceProvider provider =
                          new HostIdResourceProvider(
                              () -> HostIdResourceProvider.OsType.LINUX, testCase.pathReader, null);

                      assertHostId(testCase.expectedValue, provider);
                    }))
        .collect(Collectors.toList());
  }

  @TestFactory
  Collection<DynamicTest> createResourceWindows() {
    return Stream.of(
            new WindowsTestCase(
                "default",
                "test",
                () ->
                    new HostIdResourceProvider.ExecResult(
                        0,
                        Arrays.asList(
                            "HKEY_LOCAL_MACHINE\\SOFTWARE\\Microsoft\\Cryptography",
                            "    MachineGuid    REG_SZ    test"))),
            new WindowsTestCase(
                "error code",
                null,
                () -> new HostIdResourceProvider.ExecResult(1, Collections.emptyList())),
            new WindowsTestCase(
                "short output",
                null,
                () -> new HostIdResourceProvider.ExecResult(0, Collections.emptyList())))
        .map(
            testCase ->
                DynamicTest.dynamicTest(
                    testCase.name,
                    () -> {
                      HostIdResourceProvider provider =
                          new HostIdResourceProvider(
                              () -> HostIdResourceProvider.OsType.WINDOWS,
                              null,
                              testCase.queryWindowsRegistry);

                      assertHostId(testCase.expectedValue, provider);
                    }))
        .collect(Collectors.toList());
  }

  private static void assertHostId(String expectedValue, HostIdResourceProvider provider) {
    MapAssert<AttributeKey<?>, Object> that =
        assertThat(provider.createResource(null).getAttributes().asMap());

    if (expectedValue == null) {
      that.isEmpty();
    } else {
      that.containsEntry(HostIdResourceProvider.HOST_ID, expectedValue);
    }
  }

  @Test
  void shouldApply() {
    HostIdResourceProvider provider = new HostIdResourceProvider();
    assertThat(
            provider.shouldApply(
                DefaultConfigProperties.createFromMap(Collections.emptyMap()),
                Resource.getDefault()))
        .isTrue();
    assertThat(
            provider.shouldApply(
                DefaultConfigProperties.createFromMap(
                    Collections.singletonMap("otel.resource.attributes", "host.id=foo")),
                null))
        .isFalse();
  }
}
