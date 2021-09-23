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
   * @param startEpochNanos epoch timestamp in nanos indicating when the histogram was created.
   * @param epochNanos epoch timestamp in nanos indicating when the data was collected.
   * @param attributes attributes associated with this data point.
   * @param scale Scale characterises the resolution of the histogram, with larger values of scale
   *     offering greater precision. Bucket boundaries of the histogram are located at integer
   *     powers of the base, where <code>base = Math.pow(2, Math.pow(2, -scale))</code>.
   * @param sum The sum of all measurements in the histogram.
   * @param zeroCount Number of values that are zero.
   * @param positiveBuckets Buckets that measure positive values.
   * @param negativeBuckets Buckets that measure negative values.
   * @param exemplars List of exemplars collected from measurements that were used to form the data
   *     point.
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
        exemplars,
        scale,
        sum,
        count,
        base,
        zeroCount,
        positiveBuckets,
        negativeBuckets);
  }

  /**
   * Scale characterises the resolution of the histogram, with larger values of scale offering
   * greater precision. Bucket boundaries of the histogram are located at integer powers of the
   * base, where <code>base = Math.pow(2, Math.pow(2, -scale))</code>.
   *
   * @return the scale.
   */
  public abstract int getScale();

  /**
   * Returns the sum of all measurements in the data point. The sum should be disregarded if there
   * are both positive and negative measurements.
   *
   * @return the sum of all measurements in this data point.
   */
  public abstract double getSum();

  /**
   * Returns the number of measurements taken for this data point, including the positive bucket
   * counts, negative bucket counts, and the zero count.
   *
   * @return the number of measurements in this data point.
   */
  public abstract long getCount();

  /**
   * Returns the base, which is calculated via the scale {@link #getScale()}. The larger the base,
   * the further away bucket boundaries are from each other.
   *
   * <p><code>base = 2^(2^-scale)</code>
   *
   * @return the base.
   */
  public abstract double getBase();

  /**
   * Returns the number of measurements equal to zero in this data point.
   *
   * @return the number of values equal to zero.
   */
  public abstract long getZeroCount();

  /**
   * Return the {@link DoubleExponentialHistogramBuckets} representing the positive measurements
   * taken for this histogram.
   *
   * @return the positive buckets.
   */
  public abstract DoubleExponentialHistogramBuckets getPositiveBuckets();

  /**
   * Return the {@link DoubleExponentialHistogramBuckets} representing the negative measurements
   * taken for this histogram.
   *
   * @return the negative buckets.
   */
  public abstract DoubleExponentialHistogramBuckets getNegativeBuckets();
}
