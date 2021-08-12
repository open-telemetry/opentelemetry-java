/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import org.assertj.core.api.Assertions;

/** Asserts for (deprecated) Summary points. */
public class DoubleSummaryPointDataAssert
    extends AbstractPointDataAssert<DoubleSummaryPointDataAssert, DoubleSummaryPointData> {
  protected DoubleSummaryPointDataAssert(DoubleSummaryPointData actual) {
    super(actual, DoubleSummaryPointDataAssert.class);
  }

  /** Ensure the summary has seen the expected count of measurements. */
  public DoubleSummaryPointDataAssert hasCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getCount()).as("count").isEqualTo(expected);
    return this;
  }

  /** Ensure the summary has the expected sum across all observed measurements. */
  public DoubleSummaryPointDataAssert hasSum(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isEqualTo(expected);
    return this;
  }

  /** Ensure the summary has exactly, in any order, the given percentile values. */
  public DoubleSummaryPointDataAssert hasPercentileValues(ValueAtPercentile... percentiles) {
    isNotNull();
    Assertions.assertThat(actual.getPercentileValues()).containsExactlyInAnyOrder(percentiles);
    return this;
  }
}
