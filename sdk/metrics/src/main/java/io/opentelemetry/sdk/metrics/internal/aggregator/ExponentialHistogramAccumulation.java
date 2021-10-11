/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;

@AutoValue
abstract class ExponentialHistogramAccumulation {
  ExponentialHistogramAccumulation() {}

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

  static ExponentialHistogramAccumulation create(
      int scale,
      double sum,
      @Nonnull DoubleExponentialHistogramBuckets positiveBuckets,
      @Nonnull DoubleExponentialHistogramBuckets negativeBuckets,
      long zeroCount) {
    return create(scale, sum, positiveBuckets, negativeBuckets, zeroCount, Collections.emptyList());
  }

  abstract int getScale();

  abstract double getSum();

  abstract DoubleExponentialHistogramBuckets getPositiveBuckets();

  abstract DoubleExponentialHistogramBuckets getNegativeBuckets();

  abstract long getZeroCount();

  abstract List<ExemplarData> getExemplars();
}
