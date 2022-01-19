/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounterFactory;

/** The configuration for how to create exponential histogram buckets. */
interface ExponentialBucketStrategy {
  /** Constructs fresh new buckets. */
  DoubleExponentialHistogramBuckets newBuckets();

  /** Constructs "empty" count buckets using settings from previous recorded bucket. */
  DoubleExponentialHistogramBuckets zeroBucketFrom(DoubleExponentialHistogramBuckets old);

  static ExponentialBucketStrategy newStrategy(
      int scale, int maxBuckets, ExponentialCounterFactory counterFactory) {
    return new ExponentialBucketStrategy() {
      @Override
      public DoubleExponentialHistogramBuckets newBuckets() {
        return new DoubleExponentialHistogramBuckets(scale, maxBuckets, counterFactory);
      }

      @Override
      public DoubleExponentialHistogramBuckets zeroBucketFrom(
          DoubleExponentialHistogramBuckets old) {
        return DoubleExponentialHistogramBuckets.zeroBucketFrom(old);
      }
    };
  }
}
