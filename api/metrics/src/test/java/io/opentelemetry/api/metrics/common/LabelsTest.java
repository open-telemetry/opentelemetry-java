/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class LabelsTest {

  @Test
  void forEach() {
    final Map<String, String> entriesSeen = new LinkedHashMap<>();

    Labels labels =
        Labels.of(
            "key1", "value1",
            "key2", "value2");

    labels.forEach(entriesSeen::put);

    assertThat(entriesSeen).containsExactly(entry("key1", "value1"), entry("key2", "value2"));
  }

  @Test
  void asMap() {
    Labels labels =
        Labels.of(
            "key1", "value1",
            "key2", "value2");

    assertThat(labels.asMap()).containsExactly(entry("key1", "value1"), entry("key2", "value2"));
  }

  @Test
  void forEach_singleAttribute() {
    final Map<String, String> entriesSeen = new HashMap<>();

    Labels labels = Labels.of("key", "value");
    labels.forEach(entriesSeen::put);

    assertThat(entriesSeen).containsExactly(entry("key", "value"));
  }

  @Test
  void forEach_empty() {
    final AtomicBoolean sawSomething = new AtomicBoolean(false);
    Labels emptyLabels = Labels.empty();
    emptyLabels.forEach((key, value) -> sawSomething.set(true));
    assertThat(sawSomething.get()).isFalse();
  }

  @Test
  void orderIndependentEquality() {
    Labels one =
        Labels.of(
            "key3", "value3",
            "key1", "value1",
            "key2", "value2");
    Labels two =
        Labels.of(
            "key2", "value2",
            "key3", "value3",
            "key1", "value1");

    assertThat(one).isEqualTo(two);
  }

  @Test
  void deduplication() {
    Labels one =
        Labels.of(
            "key1", "valueX",
            "key1", "value1");
    Labels two = Labels.of("key1", "value1");

    assertThat(one).isEqualTo(two);
  }

  @Test
  void threeLabels() {
    Labels one =
        Labels.of(
            "key1", "value1",
            "key3", "value3",
            "key2", "value2");
    assertThat(one).isNotNull();
  }

  @Test
  void fourLabels() {
    Labels one =
        Labels.of(
            "key1", "value1",
            "key2", "value2",
            "key3", "value3",
            "key4", "value4");
    assertThat(one).isNotNull();
  }

  @Test
  void builder() {
    Labels labels =
        Labels.builder()
            .put("key1", "duplicateShouldBeIgnored")
            .put("key1", "value1")
            .put("key2", "value2")
            .build();

    assertThat(labels)
        .isEqualTo(
            Labels.of(
                "key1", "value1",
                "key2", "value2"));
  }

  @Test
  void toBuilder() {
    Labels initial = Labels.of("one", "a");
    Labels second = initial.toBuilder().put("two", "b").build();
    assertThat(initial.size()).isEqualTo(1);
    assertThat(second.size()).isEqualTo(2);
    assertThat(initial).isEqualTo(Labels.of("one", "a"));
    assertThat(second).isEqualTo(Labels.of("one", "a", "two", "b"));
  }
}
