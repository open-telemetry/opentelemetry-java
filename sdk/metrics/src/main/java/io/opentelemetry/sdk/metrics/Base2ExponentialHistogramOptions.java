/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

/**
 * Options for configuring base-2 exponential histogram aggregations.
 *
 * @see Aggregation#base2ExponentialBucketHistogram(Base2ExponentialHistogramOptions)
 */
public final class Base2ExponentialHistogramOptions {

  /** The default maximum number of buckets. */
  public static final int DEFAULT_MAX_BUCKETS = 160;

  /** The default maximum scale. */
  public static final int DEFAULT_MAX_SCALE = 20;

  private final int maxBuckets;
  private final int maxScale;
  private final boolean recordMinMax;

  private Base2ExponentialHistogramOptions(Builder builder) {
    this.maxBuckets = builder.maxBuckets;
    this.maxScale = builder.maxScale;
    this.recordMinMax = builder.recordMinMax;
  }

  /** Returns a new {@link Builder} for {@link Base2ExponentialHistogramOptions}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the max number of positive and negative buckets. Defaults to {@value
   * #DEFAULT_MAX_BUCKETS}.
   */
  public int maxBuckets() {
    return maxBuckets;
  }

  /** Returns the maximum and initial scale. Defaults to {@value #DEFAULT_MAX_SCALE}. */
  public int maxScale() {
    return maxScale;
  }

  /** Returns whether min and max values should be recorded. Defaults to {@code true}. */
  public boolean recordMinMax() {
    return recordMinMax;
  }

  /** Builder for {@link Base2ExponentialHistogramOptions}. */
  public static final class Builder {

    private int maxBuckets = DEFAULT_MAX_BUCKETS;
    private int maxScale = DEFAULT_MAX_SCALE;
    private boolean recordMinMax = true;

    private Builder() {}

    /**
     * Sets the max number of positive buckets and negative buckets (max total buckets is 2 * {@code
     * maxBuckets} + 1 zero bucket).
     *
     * @param maxBuckets the maximum number of buckets
     */
    public Builder setMaxBuckets(int maxBuckets) {
      this.maxBuckets = maxBuckets;
      return this;
    }

    /**
     * Sets the maximum and initial scale. If measurements can't fit in a particular scale given the
     * {@code maxBuckets}, the scale is reduced until the measurements can be accommodated. Setting
     * maxScale may reduce the number of downscales. Additionally, the performance of computing
     * bucket index is improved when scale is {@code <= 0}.
     *
     * @param maxScale the maximum scale
     */
    public Builder setMaxScale(int maxScale) {
      this.maxScale = maxScale;
      return this;
    }

    /**
     * Sets whether min and max values should be recorded.
     *
     * @param recordMinMax whether to record min and max values
     */
    public Builder setRecordMinMax(boolean recordMinMax) {
      this.recordMinMax = recordMinMax;
      return this;
    }

    /**
     * Returns a new {@link Base2ExponentialHistogramOptions} with the configuration of this
     * builder.
     */
    public Base2ExponentialHistogramOptions build() {
      return new Base2ExponentialHistogramOptions(this);
    }
  }
}
