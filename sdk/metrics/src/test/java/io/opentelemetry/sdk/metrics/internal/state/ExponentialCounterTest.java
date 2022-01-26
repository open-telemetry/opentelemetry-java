/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.state;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class ExponentialCounterTest {

  static Stream<ExponentialCounterFactory> counterProviders() {
    return Stream.of(
        ExponentialCounterFactory.mapCounter(), ExponentialCounterFactory.circularBufferCounter());
  }

  @ParameterizedTest
  @MethodSource("counterProviders")
  void expandLower(ExponentialCounterFactory counterFactory) {
    ExponentialCounter counter = counterFactory.newCounter(320);
    assertThat(counter.increment(10, 1)).isTrue();
    // Add BEFORE the initial see (array index 0) and make sure we wrap around the datastructure.
    assertThat(counter.increment(0, 1)).isTrue();
    assertThat(counter.get(10)).isEqualTo(1);
    assertThat(counter.get(0)).isEqualTo(1);
    assertThat(counter.getIndexStart()).as("index start").isEqualTo(0);
    assertThat(counter.getIndexEnd()).as("index end").isEqualTo(10);
    // Add AFTER initial entry and just push back end.
    assertThat(counter.increment(20, 1)).isTrue();
    assertThat(counter.get(20)).isEqualTo(1);
    assertThat(counter.get(10)).isEqualTo(1);
    assertThat(counter.get(0)).isEqualTo(1);
    assertThat(counter.getIndexStart()).isEqualTo(0);
    assertThat(counter.getIndexEnd()).isEqualTo(20);
  }

  @ParameterizedTest
  @MethodSource("counterProviders")
  void shouldFailAtLimit(ExponentialCounterFactory counterFactory) {
    ExponentialCounter counter = counterFactory.newCounter(320);
    assertThat(counter.increment(0, 1)).isTrue();
    assertThat(counter.increment(319, 1)).isTrue();
    // Check state
    assertThat(counter.getIndexStart()).as("index start").isEqualTo(0);
    assertThat(counter.getIndexEnd()).as("index start").isEqualTo(319);
    assertThat(counter.get(0)).as("counter[0]").isEqualTo(1);
    assertThat(counter.get(319)).as("counter[319]").isEqualTo(1);
    // Adding over the maximum # of buckets
    assertThat(counter.increment(3000, 1)).isFalse();
  }

  @ParameterizedTest
  @MethodSource("counterProviders")
  void shouldCopyCounters(ExponentialCounterFactory counterFactory) {
    ExponentialCounter counter = counterFactory.newCounter(2);
    assertThat(counter.increment(2, 1)).isTrue();
    assertThat(counter.increment(1, 1)).isTrue();
    assertThat(counter.increment(3, 1)).isFalse();

    ExponentialCounter copy = counterFactory.copy(counter);
    assertThat(counter.get(2)).as("counter[2]").isEqualTo(1);
    assertThat(copy.get(2)).as("copy[2]").isEqualTo(1);
    assertThat(copy.getMaxSize()).isEqualTo(counter.getMaxSize());
    assertThat(copy.getIndexStart()).isEqualTo(counter.getIndexStart());
    assertThat(copy.getIndexEnd()).isEqualTo(counter.getIndexEnd());
    // Mutate copy and make sure original is unchanged.
    assertThat(copy.increment(2, 1)).isTrue();
    assertThat(copy.get(2)).as("copy[2]").isEqualTo(2);
    assertThat(counter.get(2)).as("counter[2]").isEqualTo(1);
  }

  @ParameterizedTest
  @MethodSource("counterProviders")
  void shouldCopyMapCounters(ExponentialCounterFactory counterFactory) {
    ExponentialCounter counter = ExponentialCounterFactory.mapCounter().newCounter(2);
    assertThat(counter.increment(2, 1)).isTrue();
    assertThat(counter.increment(1, 1)).isTrue();
    assertThat(counter.increment(3, 1)).isFalse();

    ExponentialCounter copy = counterFactory.copy(counter);
    assertThat(counter.get(2)).as("counter[2]").isEqualTo(1);
    assertThat(copy.get(2)).as("copy[2]").isEqualTo(1);
    assertThat(copy.getMaxSize()).isEqualTo(counter.getMaxSize());
    assertThat(copy.getIndexStart()).isEqualTo(counter.getIndexStart());
    assertThat(copy.getIndexEnd()).isEqualTo(counter.getIndexEnd());
    // Mutate copy and make sure original is unchanged.
    assertThat(copy.increment(2, 1)).isTrue();
    assertThat(copy.get(2)).as("copy[2]").isEqualTo(2);
    assertThat(counter.get(2)).as("counter[2]").isEqualTo(1);
  }

  @ParameterizedTest
  @MethodSource("counterProviders")
  void shouldCopyCircularBufferCounters(ExponentialCounterFactory counterFactory) {
    ExponentialCounter counter = ExponentialCounterFactory.circularBufferCounter().newCounter(2);
    assertThat(counter.increment(2, 1)).isTrue();
    assertThat(counter.increment(1, 1)).isTrue();
    assertThat(counter.increment(3, 1)).isFalse();

    ExponentialCounter copy = counterFactory.copy(counter);
    assertThat(counter.get(2)).as("counter[2]").isEqualTo(1);
    assertThat(copy.get(2)).as("copy[2]").isEqualTo(1);
    assertThat(copy.getMaxSize()).isEqualTo(counter.getMaxSize());
    assertThat(copy.getIndexStart()).isEqualTo(counter.getIndexStart());
    assertThat(copy.getIndexEnd()).isEqualTo(counter.getIndexEnd());
    // Mutate copy and make sure original is unchanged.
    assertThat(copy.increment(2, 1)).isTrue();
    assertThat(copy.get(2)).as("copy[2]").isEqualTo(2);
    assertThat(counter.get(2)).as("counter[2]").isEqualTo(1);
  }
}
