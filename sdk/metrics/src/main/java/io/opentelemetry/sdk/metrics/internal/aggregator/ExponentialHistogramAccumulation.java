/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import java.util.List;

@AutoValue
abstract class ExponentialHistogramAccumulation {
  ExponentialHistogramAccumulation() {}

  /** Creates a new {@link ExponentialHistogramAccumulation} with the given values. */
  static ExponentialHistogramAccumulation create(
      int scale,
      double sum,
      boolean hasMinMax,
      double min,
      double max,
      DoubleExponentialHistogramBuckets positiveBuckets,
      DoubleExponentialHistogramBuckets negativeBuckets,
      long zeroCount,
      List<DoubleExemplarData> exemplars) {
    return new AutoValue_ExponentialHistogramAccumulation(
        scale, sum, hasMinMax, min, max, positiveBuckets, negativeBuckets, zeroCount, exemplars);
  }

  abstract int getScale();

  abstract double getSum();

  abstract boolean hasMinMax();

  abstract double getMin();

  abstract double getMax();

  abstract DoubleExponentialHistogramBuckets getPositiveBuckets();

  abstract DoubleExponentialHistogramBuckets getNegativeBuckets();

  abstract long getZeroCount();

  abstract List<DoubleExemplarData> getExemplars();
}
