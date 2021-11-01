/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.assertj.metrics;

import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import org.assertj.core.api.Assertions;

/** Test assertions for {@link ExponentialHistogramPointData}. */
public class ExponentialHistogramPointDataAssert
    extends AbstractPointDataAssert<
        ExponentialHistogramPointDataAssert, ExponentialHistogramPointData> {
  protected ExponentialHistogramPointDataAssert(ExponentialHistogramPointData actual) {
    super(actual, ExponentialHistogramPointDataAssert.class);
  }

  /** Ensures the {@code sum} field matches the expected value. */
  public ExponentialHistogramPointDataAssert hasSum(double expected) {
    isNotNull();
    Assertions.assertThat(actual.getSum()).as("sum").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code totalCount} field matches the expected value. */
  public ExponentialHistogramPointDataAssert hasTotalCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getCount()).as("count").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code scale} field matches the expected value. */
  public ExponentialHistogramPointDataAssert hasScale(int expected) {
    isNotNull();
    Assertions.assertThat(actual.getScale()).as("scale").isEqualTo(expected);
    return this;
  }

  /** Ensures the {@code zeroCount} field matches the expected value. */
  public ExponentialHistogramPointDataAssert hasZeroCount(long expected) {
    isNotNull();
    Assertions.assertThat(actual.getZeroCount()).as("zeroCount").isEqualTo(expected);
    return this;
  }
}
