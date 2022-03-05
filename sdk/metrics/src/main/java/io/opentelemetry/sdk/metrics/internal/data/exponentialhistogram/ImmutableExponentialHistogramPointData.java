/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * Simple auto value implementation of {@link ExponentialHistogramPointData}. For detailed javadoc
 * on the type, see {@link ExponentialHistogramPointData}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@AutoValue
@Immutable
abstract class ImmutableExponentialHistogramPointData implements ExponentialHistogramPointData {

  ImmutableExponentialHistogramPointData() {}

  /**
   * Create a DoubleExponentialHistogramPointData.
   *
   * @return a DoubleExponentialHistogramPointData.
   */
  static ImmutableExponentialHistogramPointData create(
      int scale,
      double sum,
      long zeroCount,
      ExponentialHistogramBuckets positiveBuckets,
      ExponentialHistogramBuckets negativeBuckets,
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<ExemplarData> exemplars) {

    long count = zeroCount + positiveBuckets.getTotalCount() + negativeBuckets.getTotalCount();

    return new AutoValue_ImmutableExponentialHistogramPointData(
        scale,
        sum,
        count,
        zeroCount,
        positiveBuckets,
        negativeBuckets,
        startEpochNanos,
        epochNanos,
        attributes,
        exemplars);
  }

  @Override
  public abstract int getScale();

  @Override
  public abstract double getSum();

  @Override
  public abstract long getCount();

  @Override
  public abstract long getZeroCount();

  @Override
  public abstract ExponentialHistogramBuckets getPositiveBuckets();

  @Override
  public abstract ExponentialHistogramBuckets getNegativeBuckets();

  @Override
  public abstract long getStartEpochNanos();

  @Override
  public abstract long getEpochNanos();

  @Override
  public abstract Attributes getAttributes();

  @Override
  public abstract List<ExemplarData> getExemplars();
}
