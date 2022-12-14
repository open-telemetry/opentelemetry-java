/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.metrics.internal.state.ExponentialCounterFactory;

/** The configuration for how to create exponential histogram buckets. */
final class ExponentialBucketStrategy {

  private static final int DEFAULT_STARTING_SCALE = 20;

  private final int startingScale;
  /** The maximum number of buckets that will be used for positive or negative recordings. */
  private final int maxBuckets;
  /** The mechanism of constructing and copying buckets. */
  private final ExponentialCounterFactory counterFactory;

  private ExponentialBucketStrategy(
      int startingScale, int maxBuckets, ExponentialCounterFactory counterFactory) {
    this.startingScale = startingScale;
    this.maxBuckets = maxBuckets;
    this.counterFactory = counterFactory;
  }

  /** Constructs fresh new buckets with default settings. */
  DoubleExponentialHistogramBuckets newBuckets() {
    return new DoubleExponentialHistogramBuckets(startingScale, maxBuckets, counterFactory);
  }

  int getStartingScale() {
    return startingScale;
  }

  /** Create a new strategy for generating Exponential Buckets. */
  static ExponentialBucketStrategy newStrategy(
      int maxBuckets, ExponentialCounterFactory counterFactory) {
    return new ExponentialBucketStrategy(DEFAULT_STARTING_SCALE, maxBuckets, counterFactory);
  }

  /** Create a new strategy for generating Exponential Buckets. */
  static ExponentialBucketStrategy newStrategy(
      int maxBuckets, ExponentialCounterFactory counterFactory, int startingScale) {
    return new ExponentialBucketStrategy(startingScale, maxBuckets, counterFactory);
  }
}
