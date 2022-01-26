/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounterFactory;

/** The configuration for how to create exponential histogram buckets. */
final class ExponentialBucketStrategy {
  /** Starting scale of exponential buckets. */
  private final int scale;
  /** The maximum number of buckets that will be used for positive or negative recordings. */
  private final int maxBuckets;
  /** The mechanism of constructing and copying buckets. */
  private final ExponentialCounterFactory counterFactory;

  private ExponentialBucketStrategy(
      int scale, int maxBuckets, ExponentialCounterFactory counterFactory) {
    this.scale = scale;
    this.maxBuckets = maxBuckets;
    this.counterFactory = counterFactory;
  }

  /** Constructs fresh new buckets with default settings. */
  DoubleExponentialHistogramBuckets newBuckets() {
    return new DoubleExponentialHistogramBuckets(scale, maxBuckets, counterFactory);
  }

  /** Create a new strategy for generating Exponential Buckets. */
  static ExponentialBucketStrategy newStrategy(
      int scale, int maxBuckets, ExponentialCounterFactory counterFactory) {
    return new ExponentialBucketStrategy(scale, maxBuckets, counterFactory);
  }
}
