/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.common.Attributes;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * DoubleExponentialHistogramPointData represents an approximate distribution of measurements across
 * exponentially increasing bucket boundaries, taken for a {@link DoubleExponentialHistogramData}.
 * It contains the necessary information to calculate bucket boundaries and perform aggregation.
 *
 * <p>The bucket boundaries are calculated using both the scale {@link #getScale()}, and the offset
 * {@link DoubleExponentialHistogramBuckets#getOffset()}.
 *
 * <p>See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#exponentialhistogram
 */
@Immutable
@AutoValue
public abstract class DoubleExponentialHistogramPointData implements PointData {

  DoubleExponentialHistogramPointData() {}

  /**
   * Creates a DoubleExponentialHistogramPointData.
   *
   * @param scale Scale characterises the resolution of the histogram, with larger values of scale
   *     offering greater precision. Bucket boundaries of the histogram are located at integer
   *     powers of the base, where <code>base = Math.pow(2, Math.pow(2, -scale))</code>.
   * @param sum The sum of all measurements in the histogram.
   * @param zeroCount Number of values that are zero.
   * @param positiveBuckets Buckets with positive values.
   * @param negativeBuckets Buckets with negative values.
   * @return a DoubleExponentialHistogramPointData
   */
  public static DoubleExponentialHistogramPointData create(
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      int scale,
      double sum,
      long zeroCount,
      DoubleExponentialHistogramBuckets positiveBuckets,
      DoubleExponentialHistogramBuckets negativeBuckets,
      List<Exemplar> exemplars) {

    long count = zeroCount + positiveBuckets.getTotalCount() + negativeBuckets.getTotalCount();
    double base = Math.pow(2, Math.pow(2, -scale));

    return new AutoValue_DoubleExponentialHistogramPointData(
        startEpochNanos,
        epochNanos,
        attributes,
        scale,
        sum,
        count,
        base,
        zeroCount,
        positiveBuckets,
        negativeBuckets,
        exemplars);
  }

  @Override
  public abstract long getStartEpochNanos();

  @Override
  public abstract long getEpochNanos();

  @Override
  public abstract Attributes getAttributes();

  /**
   * Scale characterises the resolution of the histogram, with larger values of scale offering
   * greater precision. Bucket boundaries of the histogram are located at integer powers of the
   * base, where <code>base = Math.pow(2, Math.pow(2, -scale))</code>.
   *
   * @return the scale.
   */
  public abstract int getScale();

  public abstract double getSum();

  public abstract long getCount();

  public abstract double getBase();

  public abstract long getZeroCount();

  public abstract DoubleExponentialHistogramBuckets getPositiveBuckets();

  public abstract DoubleExponentialHistogramBuckets getNegativeBuckets();

  @Override
  public abstract List<Exemplar> getExemplars();
}
