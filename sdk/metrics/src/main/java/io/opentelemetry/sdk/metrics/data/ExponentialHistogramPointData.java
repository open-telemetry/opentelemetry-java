/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.data;

import io.opentelemetry.api.common.Attributes;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * ExponentialHistogramPointData represents an approximate distribution of measurements across
 * exponentially increasing bucket boundaries, taken for a {@link ExponentialHistogramData}. It
 * contains the necessary information to calculate bucket boundaries and perform aggregation.
 *
 * <p>The bucket boundaries are calculated using both the scale {@link #getScale()}, and the offset
 * {@link ExponentialHistogramBuckets#getOffset()}.
 *
 * <p>See:
 * https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/datamodel.md#exponentialhistogram
 */
@Immutable
public interface ExponentialHistogramPointData extends PointData {

  /**
   * Create an ExponentialHistogramPointData.
   *
   * @return an ExponentialHistogramPointData.
   */
  static ExponentialHistogramPointData create(
      int scale,
      double sum,
      long zeroCount,
      ExponentialHistogramBuckets positiveBuckets,
      ExponentialHistogramBuckets negativeBuckets,
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<ExemplarData> exemplars) {

    return DoubleExponentialHistogramPointData.create(
        scale,
        sum,
        zeroCount,
        positiveBuckets,
        negativeBuckets,
        startEpochNanos,
        epochNanos,
        attributes,
        exemplars);
  }

  /**
   * Scale characterises the resolution of the histogram, with larger values of scale offering
   * greater precision. Bucket boundaries of the histogram are located at integer powers of the
   * base, where <code>base = Math.pow(2, Math.pow(2, -scale))</code>.
   *
   * @return the scale.
   */
  int getScale();

  /**
   * Returns the sum of all measurements in the data point. The sum should be disregarded if there
   * are both positive and negative measurements.
   *
   * @return the sum of all measurements in this data point.
   */
  double getSum();

  /**
   * Returns the number of measurements taken for this data point, including the positive bucket
   * counts, negative bucket counts, and the zero count.
   *
   * @return the number of measurements in this data point.
   */
  long getCount();

  /**
   * Returns the number of measurements equal to zero in this data point.
   *
   * @return the number of values equal to zero.
   */
  long getZeroCount();

  /**
   * Return the {@link ExponentialHistogramBuckets} representing the positive measurements taken for
   * this histogram.
   *
   * @return the positive buckets.
   */
  ExponentialHistogramBuckets getPositiveBuckets();

  /**
   * Return the {@link ExponentialHistogramBuckets} representing the negative measurements taken for
   * this histogram.
   *
   * @return the negative buckets.
   */
  ExponentialHistogramBuckets getNegativeBuckets();
}
