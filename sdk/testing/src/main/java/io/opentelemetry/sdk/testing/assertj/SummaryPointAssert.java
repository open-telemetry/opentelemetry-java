/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import java.util.Arrays;
import java.util.function.Consumer;
import org.assertj.core.api.Assertions;

/**
 * Test assertions for {@link SummaryPointData}.
 *
 * @since 1.14.0
 */
public final class SummaryPointAssert
    extends AbstractPointAssert<SummaryPointAssert, SummaryPointData> {

  SummaryPointAssert(SummaryPointData actual) {
    super(actual, SummaryPointAssert.class);
  }

  /** Asserts the summary has seen the expected count of measurements. */
  public SummaryPointAssert hasCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getCount()).as("count").isEqualTo(expected);
    return this;
  }

  /** Asserts the summary has the expected sum across all observed measurements. */
  public SummaryPointAssert hasSum(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isEqualTo(expected);
    return this;
  }

  /**
   * Asserts the point has values matching all of the given assertions and no more, in any order.
   */
  @SafeVarargs
  @SuppressWarnings("varargs")
  public final SummaryPointAssert hasValuesSatisfying(
      Consumer<ValueAtQuantileAssert>... assertions) {
    return hasValuesSatisfying(Arrays.asList(assertions));
  }

  /**
   * Asserts the point has values matching all of the given assertions and no more, in any order.
   */
  public SummaryPointAssert hasValuesSatisfying(
      Iterable<? extends Consumer<ValueAtQuantileAssert>> assertions) {
    assertThat(actual.getValues())
        .satisfiesExactlyInAnyOrder(AssertUtil.toConsumers(assertions, ValueAtQuantileAssert::new));
    return this;
  }
}
