/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Utilities for interacting with explicit bucket histograms.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ExplicitBucketHistogramUtils {
  private ExplicitBucketHistogramUtils() {}

  public static final List<Double> DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES =
      Collections.unmodifiableList(
          Arrays.asList(
              5d, 10d, 25d, 50d, 75d, 100d, 250d, 500d, 750d, 1_000d, 2_500d, 5_000d, 7_500d,
              10_000d));

  /** Converts bucket boundary "convenient" configuration into the "more efficient" array. */
  public static double[] createBoundaryArray(List<Double> boundaries) {
    return boundaries.stream().mapToDouble(i -> i).toArray();
  }

  /**
   * Finds the bucket index for a histogram.
   *
   * @param boundaries the array of bucket boundaries.
   * @param value The current measurement value
   * @return The bucket index where the value should be recorded.
   */
  public static int findBucketIndex(double[] boundaries, double value) {
    // Benchmark shows that linear search performs better than binary search with ordinary
    // buckets.
    for (int i = 0; i < boundaries.length; ++i) {
      if (value <= boundaries[i]) {
        return i;
      }
    }
    return boundaries.length;
  }
}
