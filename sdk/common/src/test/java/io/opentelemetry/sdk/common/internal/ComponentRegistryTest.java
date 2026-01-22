/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.Attributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

class ComponentRegistryTest {

  private static final String NAME = "test_name";
  private static final String VERSION = "version";
  private static final String SCHEMA_URL = "http://schema.com";
  private static final Attributes ATTRIBUTES = Attributes.builder().put("k1", "v1").build();
  private final ComponentRegistry<TestComponent> registry =
      new ComponentRegistry<>(unused -> new TestComponent());

  @Test
  void get_SameInstance() {
    assertThat(registry.get(NAME, null, null, Attributes.empty()))
        .isSameAs(registry.get(NAME, null, null, Attributes.empty()))
        .isSameAs(registry.get(NAME, null, null, Attributes.builder().put("k1", "v2").build()));

    assertThat(registry.get(NAME, VERSION, null, Attributes.empty()))
        .isSameAs(registry.get(NAME, VERSION, null, Attributes.empty()))
        .isSameAs(registry.get(NAME, VERSION, null, Attributes.builder().put("k1", "v2").build()));
    assertThat(registry.get(NAME, null, SCHEMA_URL, Attributes.empty()))
        .isSameAs(registry.get(NAME, null, SCHEMA_URL, Attributes.empty()))
        .isSameAs(
            registry.get(NAME, null, SCHEMA_URL, Attributes.builder().put("k1", "v2").build()));
    assertThat(registry.get(NAME, VERSION, SCHEMA_URL, Attributes.empty()))
        .isSameAs(registry.get(NAME, VERSION, SCHEMA_URL, Attributes.empty()))
        .isSameAs(
            registry.get(NAME, VERSION, SCHEMA_URL, Attributes.builder().put("k1", "v2").build()));
  }

  @Test
  void get_DifferentInstance() {
    assertThat(registry.get(NAME, VERSION, SCHEMA_URL, ATTRIBUTES))
        .isNotSameAs(registry.get(NAME + "_1", VERSION, SCHEMA_URL, ATTRIBUTES))
        .isNotSameAs(registry.get(NAME, VERSION + "_1", SCHEMA_URL, ATTRIBUTES))
        .isNotSameAs(registry.get(NAME, VERSION, SCHEMA_URL + "_1", ATTRIBUTES));

    assertThat(registry.get(NAME, VERSION, null, Attributes.empty()))
        .isNotSameAs(registry.get(NAME, null, null, Attributes.empty()));

    assertThat(registry.get(NAME, null, SCHEMA_URL, Attributes.empty()))
        .isNotSameAs(registry.get(NAME, null, null, Attributes.empty()));
  }

  @Test
  @SuppressWarnings("ReturnValueIgnored")
  void getComponents_HighConcurrency() throws ExecutionException, InterruptedException {
    List<Future<?>> futures = new ArrayList<>();
    Random random = new Random();
    int concurrency = 2;
    ExecutorService executor = Executors.newFixedThreadPool(concurrency);

    try {
      for (int i = 0; i < 100; i++) {
        futures.add(
            executor.submit(
                () -> {
                  String name =
                      IntStream.range(0, 20)
                          .mapToObj(unused -> String.valueOf((char) random.nextInt(26)))
                          .collect(joining());
                  registry.get(name, null, null, Attributes.empty());
                }));
        futures.add(
            executor.submit(() -> registry.getComponents().forEach(TestComponent::hashCode)));
      }

      for (Future<?> future : futures) {
        future.get();
      }
    } finally {
      executor.shutdown();
    }
  }

  private static final class TestComponent {}
}
