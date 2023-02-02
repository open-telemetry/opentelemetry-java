/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.util.Arrays;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;

/**
 * Test assertions for {@link ExponentialHistogramPointData}.
 *
 * @since 1.23.0
 */
public final class ExponentialHistogramPointAssert
    extends AbstractPointAssert<ExponentialHistogramPointAssert, ExponentialHistogramPointData> {
  ExponentialHistogramPointAssert(ExponentialHistogramPointData actual) {
    super(actual, ExponentialHistogramPointAssert.class);
  }

  /** Ensures the {@code sum} field matches the expected value. */
  public ExponentialHistogramPointAssert hasSum(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code min} field matches the expected value. */
  public ExponentialHistogramPointAssert hasMin(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getMin()).as("min").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code min} field matches the expected value. */
  public ExponentialHistogramPointAssert hasMax(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getMax()).as("max").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code count} field matches the expected value. */
  public ExponentialHistogramPointAssert hasCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getCount()).as("count").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code scale} field matches the expected value. */
  public ExponentialHistogramPointAssert hasScale(int expected) {
    isNotNull();
    Assertions.assertThat(actual.getScale()).as("scale").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code zeroCount} field matches the expected value. */
  public ExponentialHistogramPointAssert hasZeroCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getZeroCount()).as("zeroCount").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code positiveBuckets} field satisfies the provided assertion. */
  public ExponentialHistogramPointAssert hasPositiveBucketsSatisfying(
      Consumer<ExponentialHistogramBucketsAssert> assertion) {
    isNotNull();
    assertion.accept(new ExponentialHistogramBucketsAssert(actual.getPositiveBuckets()));
    return this;
  }

  /** Ensures the {@code negativeBuckets} field satisfies the provided assertion. */
  public ExponentialHistogramPointAssert hasNegativeBucketsSatisfying(
      Consumer<ExponentialHistogramBucketsAssert> assertion) {
    isNotNull();
    assertion.accept(new ExponentialHistogramBucketsAssert(actual.getNegativeBuckets()));
    return this;
  }

  /** Asserts the point has the specified exemplars, in any order. */
  public ExponentialHistogramPointAssert hasExemplars(DoubleExemplarData... exemplars) {
    isNotNull();
    Assertions.assertThat(actual.getExemplars())
        .as("exemplars")
        .containsExactlyInAnyOrder(exemplars);
    return myself;
  }

  /** Asserts the point has exemplars matching all of the assertions, in any order. */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final ExponentialHistogramPointAssert hasExemplarsSatisfying(
      Consumer<DoubleExemplarAssert>... assertions) {
    return hasExemplarsSatisfying(Arrays.asList(assertions));
  }

  /** Asserts the point has exemplars matching all of the assertions, in any order. */
  public ExponentialHistogramPointAssert hasExemplarsSatisfying(
      Iterable<? extends Consumer<DoubleExemplarAssert>> assertions) {
    isNotNull();
    assertThat(actual.getExemplars())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, DoubleExemplarAssert::new));
    return myself;
  }
}
