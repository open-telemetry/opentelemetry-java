/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AdaptingIntegerArrayTest {

  // Set of values that require specific sized arrays to hold them.
  static Stream<Long> interestingValues() {
    return Stream.of(
        1L, // Fits in byte
        Byte.MAX_VALUE + 1L, // Fits in Short
        Short.MAX_VALUE + 1L, // First in Integer
        Integer.MAX_VALUE + 1L // First in Long
        );
  }

  @ParameterizedTest
  @MethodSource("interestingValues")
  void testSize(long value) {
    AdaptingIntegerArray counter = new AdaptingIntegerArray(10);
    assertThat(counter.length()).isEqualTo(10);
    // Force array to change size, make sure size is the same.
    counter.increment(0, value);
    assertThat(counter.length()).isEqualTo(10);
  }

  @ParameterizedTest
  @MethodSource("interestingValues")
  void testIncrementAndGet(long value) {
    AdaptingIntegerArray counter = new AdaptingIntegerArray(10);
    for (int idx = 0; idx < 10; idx += 1) {
      assertThat(counter.get(idx)).isEqualTo(0);
      counter.increment(idx, 1);
      assertThat(counter.get(idx)).isEqualTo(1);
      counter.increment(idx, value);
      assertThat(counter.get(idx)).isEqualTo(value + 1);
    }
  }

  @Test
  void testHandlesLongValues() {
    AdaptingIntegerArray counter = new AdaptingIntegerArray(1);
    assertThat(counter.get(0)).isEqualTo(0);
    long expected = Integer.MAX_VALUE + 1L;
    counter.increment(0, expected);
    assertThat(counter.get(0)).isEqualTo(expected);
  }

  @ParameterizedTest
  @MethodSource("interestingValues")
  void testCopy(long value) {
    AdaptingIntegerArray counter = new AdaptingIntegerArray(1);
    counter.increment(0, value);
    assertThat(counter.get(0)).isEqualTo(value);

    AdaptingIntegerArray copy = counter.copy();
    assertThat(copy.get(0)).isEqualTo(value);

    counter.increment(0, 1);
    assertThat(counter.get(0)).isEqualTo(value + 1);
    assertThat(copy.get(0)).isEqualTo(value);
  }

  @ParameterizedTest
  @MethodSource("interestingValues")
  void testClear(long value) {
    AdaptingIntegerArray counter = new AdaptingIntegerArray(1);
    counter.increment(0, value);
    assertThat(counter.get(0)).isEqualTo(value);

    counter.clear();
    counter.increment(0, 1);
    assertThat(counter.get(0)).isEqualTo(1);
  }
}
