/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Options for configuring explicit bucket histogram aggregations.
 *
 * @see Aggregation#explicitBucketHistogram(ExplicitBucketHistogramOptions)
 */
public final class ExplicitBucketHistogramOptions {

  @Nullable private final List<Double> bucketBoundaries;
  private final boolean recordMinMax;

  private ExplicitBucketHistogramOptions(Builder builder) {
    this.bucketBoundaries = builder.bucketBoundaries;
    this.recordMinMax = builder.recordMinMax;
  }

  /** Returns a new {@link Builder} for {@link ExplicitBucketHistogramOptions}. */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Returns the bucket boundaries, or {@code null} if the default OTel bucket boundaries should be
   * used.
   */
  @Nullable
  public List<Double> bucketBoundaries() {
    return bucketBoundaries;
  }

  /** Returns whether min and max values should be recorded. Defaults to {@code true}. */
  public boolean recordMinMax() {
    return recordMinMax;
  }

  /** Builder for {@link ExplicitBucketHistogramOptions}. */
  public static final class Builder {

    @Nullable private List<Double> bucketBoundaries = null;
    private boolean recordMinMax = true;

    private Builder() {}

    /**
     * Sets the bucket boundaries. If not set, the default OTel bucket boundaries are used.
     *
     * @param bucketBoundaries a list of (inclusive) upper bounds, in order from lowest to highest
     */
    public Builder setBucketBoundaries(List<Double> bucketBoundaries) {
      this.bucketBoundaries = bucketBoundaries;
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
     * Returns a new {@link ExplicitBucketHistogramOptions} with the configuration of this builder.
     */
    public ExplicitBucketHistogramOptions build() {
      return new ExplicitBucketHistogramOptions(this);
    }
  }
}
