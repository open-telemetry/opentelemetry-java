/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounterFactory;

/** The configuration for how to create exponential histogram buckets. */
final class ExponentialBucketStrategy {

  private final int maxScale;
  /** The maximum number of buckets that will be used for positive or negative recordings. */
  private final int maxBuckets;
  /** The mechanism of constructing and copying buckets. */
  private final ExponentialCounterFactory counterFactory;

  private ExponentialBucketStrategy(
      int maxScale, int maxBuckets, ExponentialCounterFactory counterFactory) {
    this.maxScale = maxScale;
    this.maxBuckets = maxBuckets;
    this.counterFactory = counterFactory;
  }

  /** Constructs fresh new buckets with default settings. */
  DoubleExponentialHistogramBuckets newBuckets() {
    return new DoubleExponentialHistogramBuckets(maxScale, maxBuckets, counterFactory);
  }

  int getMaxScale() {
    return maxScale;
  }

  /** Create a new strategy for generating Exponential Buckets. */
  static ExponentialBucketStrategy newStrategy(
      int maxBuckets, ExponentialCounterFactory counterFactory, int maxScale) {
    return new ExponentialBucketStrategy(maxScale, maxBuckets, counterFactory);
  }
}
