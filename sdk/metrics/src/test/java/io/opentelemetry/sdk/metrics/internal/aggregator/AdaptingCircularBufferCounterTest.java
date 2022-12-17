/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AdaptingCircularBufferCounterTest {

  @Test
  void returnsZeroOutsidePopulatedRange() {
    AdaptingCircularBufferCounter counter = newCounter(10);
    assertThat(counter.get(0)).isEqualTo(0);
    assertThat(counter.get(100)).isEqualTo(0);
    counter.increment(2, 1);
    counter.increment(99, 1);
    assertThat(counter.get(0)).isEqualTo(0);
    assertThat(counter.get(100)).isEqualTo(0);
  }

  @Test
  void expandLower() {
    AdaptingCircularBufferCounter counter = newCounter(160);
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

  @Test
  void shouldFailAtLimit() {
    AdaptingCircularBufferCounter counter = newCounter(160);
    assertThat(counter.increment(0, 1)).isTrue();
    assertThat(counter.increment(120, 1)).isTrue();
    // Check state
    assertThat(counter.getIndexStart()).as("index start").isEqualTo(0);
    assertThat(counter.getIndexEnd()).as("index start").isEqualTo(120);
    assertThat(counter.get(0)).as("counter[0]").isEqualTo(1);
    assertThat(counter.get(120)).as("counter[120]").isEqualTo(1);
    // Adding over the maximum # of buckets
    assertThat(counter.increment(3000, 1)).isFalse();
  }

  @Test
  void shouldCopyCounters() {
    AdaptingCircularBufferCounter counter = newCounter(2);
    assertThat(counter.increment(2, 1)).isTrue();
    assertThat(counter.increment(1, 1)).isTrue();
    assertThat(counter.increment(3, 1)).isFalse();

    AdaptingCircularBufferCounter copy = new AdaptingCircularBufferCounter(counter);
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

  private static AdaptingCircularBufferCounter newCounter(int maxBuckets) {
    return new AdaptingCircularBufferCounter(maxBuckets);
  }
}
