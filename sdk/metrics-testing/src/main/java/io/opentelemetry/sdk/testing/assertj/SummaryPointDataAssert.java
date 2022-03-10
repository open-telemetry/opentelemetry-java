/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj;

import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import org.assertj.core.api.Assertions;

/** Asserts for (deprecated) Summary points. */
public class SummaryPointDataAssert
    extends AbstractPointDataAssert<SummaryPointDataAssert, SummaryPointData> {
  protected SummaryPointDataAssert(SummaryPointData actual) {
    super(actual, SummaryPointDataAssert.class);
  }

  /** Ensure the summary has seen the expected count of measurements. */
  public SummaryPointDataAssert hasCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getCount()).as("count").isEqualTo(expected);
    return this;
  }

  /** Ensure the summary has the expected sum across all observed measurements. */
  public SummaryPointDataAssert hasSum(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isEqualTo(expected);
    return this;
  }

  /** Ensure the summary has exactly, in any order, the given percentile values. */
  public SummaryPointDataAssert hasValues(ValueAtQuantile... values) {
    isNotNull();
    Assertions.assertThat(actual.getValues()).containsExactlyInAnyOrder(values);
    return this;
  }
}
