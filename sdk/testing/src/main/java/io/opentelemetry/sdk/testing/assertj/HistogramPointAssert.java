/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import java.util.Arrays;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;

/**
 * Test assertions for {@link HistogramPointData}.
 *
 * @since 1.14.0
 */
public final class HistogramPointAssert
    extends AbstractPointAssert<HistogramPointAssert, HistogramPointData> {

  HistogramPointAssert(HistogramPointData actual) {
    super(actual, HistogramPointAssert.class);
  }

  /** Asserts the {@code sum} field matches the expected value. */
  public HistogramPointAssert hasSum(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isEqualTo(expected);
    return this;
  }

  /** Asserts the {@code sum} field contains a greater value than the passed {@code boundary}. */
  public HistogramPointAssert hasSumGreaterThan(double boundary) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isGreaterThan(boundary);
    return this;
  }

  /** Asserts the {@code min} field matches the expected value. */
  public HistogramPointAssert hasMin(double expected) {
    isNotNull();
    Assertions.assertThat(actual.hasMin()).isTrue();
    Assertions.assertThat(actual.getMin()).as("min").isEqualTo(expected);
    return this;
  }

  /** Asserts the {@code max} field matches the expected value. */
  public HistogramPointAssert hasMax(double expected) {
    isNotNull();
    Assertions.assertThat(actual.hasMax()).isTrue();
    Assertions.assertThat(actual.getMax()).as("max").isEqualTo(expected);
    return this;
  }

  /** Asserts the {@code count} field matches the expected value. */
  public HistogramPointAssert hasCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getCount()).as("count").isEqualTo(expected);
    return this;
  }

  /**
   * Asserts the {@code boundaries} field matches the expected value.
   *
   * @param boundaries The set of bucket boundaries in the same order as the expected collection.
   */
  public HistogramPointAssert hasBucketBoundaries(double... boundaries) {
    isNotNull();
    Double[] bigBoundaries = Arrays.stream(boundaries).boxed().toArray(Double[]::new);
    Assertions.assertThat(actual.getBoundaries()).as("boundaries").containsExactly(bigBoundaries);
    return this;
  }

  /**
   * Asserts the {@code counts} field matches the expected value.
   *
   * @param counts The set of bucket counts in the same order as the expected collection.
   */
  public HistogramPointAssert hasBucketCounts(long... counts) {
    isNotNull();
    Long[] bigCounts = Arrays.stream(counts).boxed().toArray(Long[]::new);
    Assertions.assertThat(actual.getCounts()).as("bucketCounts").containsExactly(bigCounts);
    return this;
  }

  /** Asserts the point has the specified exemplars, in any order. */
  public HistogramPointAssert hasExemplars(DoubleExemplarData... exemplars) {
    isNotNull();
    Assertions.assertThat(actual.getExemplars())
        .as("exemplars")
        .containsExactlyInAnyOrder(exemplars);
    return myself;
  }

  /** Asserts the point has exemplars matching all of the assertions, in any order. */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final HistogramPointAssert hasExemplarsSatisfying(
      Consumer<DoubleExemplarAssert>... assertions) {
    return hasExemplarsSatisfying(Arrays.asList(assertions));
  }

  /** Asserts the point has exemplars matching all of the assertions, in any order. */
  public HistogramPointAssert hasExemplarsSatisfying(
      Iterable<? extends Consumer<DoubleExemplarAssert>> assertions) {
    isNotNull();
    assertThat(actual.getExemplars())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, DoubleExemplarAssert::new));
    return myself;
  }
}
