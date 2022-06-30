/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.PointData;
import java.util.List;
import javax.annotation.Nullable;
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
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public interface ExponentialHistogramPointData extends PointData {

  /**
   * Create an ExponentialHistogramPointData.
   *
   * @return an ExponentialHistogramPointData.
   */
  @SuppressWarnings("TooManyParameters")
  static ExponentialHistogramPointData create(
      int scale,
      double sum,
      long zeroCount,
      @Nullable Double min,
      @Nullable Double max,
      ExponentialHistogramBuckets positiveBuckets,
      ExponentialHistogramBuckets negativeBuckets,
      long startEpochNanos,
      long epochNanos,
      Attributes attributes,
      List<DoubleExemplarData> exemplars) {

    return ImmutableExponentialHistogramPointData.create(
        scale,
        sum,
        zeroCount,
        min,
        max,
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

  /** Return {@code true} if {@link #getMin()} is set. */
  boolean hasMin();

  /**
   * The min of all measurements recorded, if {@link #hasMin()} is {@code true}. If {@link
   * #hasMin()} is {@code false}, the response should be ignored.
   */
  double getMin();

  /** Return {@code true} if {@link #getMax()} is set. */
  boolean hasMax();

  /**
   * The max of all measurements recorded, if {@link #hasMax()} is {@code true}. If {@link
   * #hasMax()} is {@code false}, the response should be ignored.
   */
  double getMax();

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

  /** List of exemplars collected from measurements that were used to form the data point. */
  @Override
  List<DoubleExemplarData> getExemplars();
}
