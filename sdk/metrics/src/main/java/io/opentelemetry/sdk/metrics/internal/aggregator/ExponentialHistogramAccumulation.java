/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

@Immutable
@AutoValue
abstract class ExponentialHistogramAccumulation {
  ExponentialHistogramAccumulation() {}

  /**
   * Creates a new {@link ExponentialHistogramAccumulation} with the given values.
   *
   * @param scale the scale of the exponential histogram.
   * @param sum the sum of all the recordings of the histogram.
   * @param positiveBuckets the buckets counting positive recordings.
   * @param negativeBuckets the buckets counting negative recordings.
   * @param zeroCount The amount of time zero was recorded.
   * @param exemplars The exemplars.
   * @return a new {@link ExponentialHistogramAccumulation} with the given values.
   */
  static ExponentialHistogramAccumulation create(
      int scale,
      double sum,
      @Nonnull DoubleExponentialHistogramBuckets positiveBuckets,
      @Nonnull DoubleExponentialHistogramBuckets negativeBuckets,
      long zeroCount,
      List<ExemplarData> exemplars) {
    return new AutoValue_ExponentialHistogramAccumulation(
        scale, sum, positiveBuckets, negativeBuckets, zeroCount, exemplars);
  }

  abstract int getScale();

  abstract double getSum();

  abstract DoubleExponentialHistogramBuckets getPositiveBuckets();

  abstract DoubleExponentialHistogramBuckets getNegativeBuckets();

  abstract long getZeroCount();

  abstract List<ExemplarData> getExemplars();
}
