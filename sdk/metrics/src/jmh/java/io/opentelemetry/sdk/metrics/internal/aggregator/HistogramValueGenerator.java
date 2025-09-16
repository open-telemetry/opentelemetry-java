/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.DoubleSupplier;

/** Methods of generating values for histogram benchmarks. */
@SuppressWarnings("ImmutableEnumChecker")
public enum HistogramValueGenerator {
  // Test scenario where we rotate around histogram buckets.
  // This is a degenerate test case where we see every next measurement in a different
  // bucket, mean to be the "optimal" explicit bucket histogram scenario.
  FIXED_BUCKET_BOUNDARIES(explicitDefaultBucketPool()),
  // Test scenario where we randomly get values between 0 and 2000.
  // Note: for millisecond latency, this would mean we expect our calls to be randomly
  // distributed between 0 and 2 seconds (not very likely).
  // This is meant to test more "worst case scenarios" where Exponential histograms must
  // expand scale factor due to highly distributed data.
  UNIFORM_RANDOM_WITHIN_2K(randomPool(20000, 2000)),
  // Test scenario where we're measuring latency with mean of 1 seconds, std deviation of a quarter
  // second.  This is our "optimised" use case.
  // Note: In practice we likely want to add several gaussian pools, as in real microsevices we
  // tend to notice some optional processing show up as additive gaussian noise with higher
  // mean/stddev.  However, this best represents a simple microservice.
  GAUSSIAN_LATENCY(randomGaussianPool(20000, 1000, 250));

  // A random seed we use to ensure tests are repeatable.
  private static final int INITIAL_SEED = 513423236;
  private final double[] pool;

  HistogramValueGenerator(double[] pool) {
    this.pool = pool;
  }

  /** Returns a supplier of doubles values. */
  public final DoubleSupplier supplier() {
    return new PoolSupplier(this.pool);
  }

  // Return values from the pool, rotating around as necessary back to the beginning.
  private static class PoolSupplier implements DoubleSupplier {
    private final double[] pool;
    private final AtomicInteger idx = new AtomicInteger(0);

    private PoolSupplier(double[] pool) {
      this.pool = pool;
    }

    @Override
    public double getAsDouble() {
      return pool[idx.incrementAndGet() % pool.length];
    }
  }

  /** Constructs a pool using explicit bucket histogram boundaries. */
  private static double[] explicitDefaultBucketPool() {
    // Add the bucket LE bucket boundaries.
    List<Double> fixedBoundaries =
        new ArrayList<>(ExplicitBucketHistogramUtils.DEFAULT_HISTOGRAM_BUCKET_BOUNDARIES);
    // Add Double max value as our other extreme.
    fixedBoundaries.add(Double.MAX_VALUE);
    return ExplicitBucketHistogramUtils.createBoundaryArray(fixedBoundaries);
  }

  /** Create a pool of random numbers within bound, and of size. */
  private static double[] randomPool(int size, double bound) {
    double[] pool = new double[size];
    Random random = new Random(INITIAL_SEED);
    for (int i = 0; i < size; i++) {
      pool[i] = random.nextDouble() * bound;
    }
    return pool;
  }

  /** Create a pool approximating a gaussian distribution w/ given mean and standard deviation. */
  private static double[] randomGaussianPool(int size, double mean, double deviation) {
    double[] pool = new double[size];
    Random random = new Random(INITIAL_SEED);
    for (int i = 0; i < size; i++) {
      pool[i] = random.nextGaussian() * deviation + mean;
    }
    return pool;
  }
}
