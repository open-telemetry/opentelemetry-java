/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.internal.Utils.checkArgument;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * Options for configuring base-2 exponential histogram aggregations.
 *
 * @see Aggregation#base2ExponentialBucketHistogram(Base2ExponentialHistogramOptions)
 */
@AutoValue
@Immutable
public abstract class Base2ExponentialHistogramOptions {

  /** The default maximum number of buckets. */
  public static final int DEFAULT_MAX_BUCKETS = 160;

  /** The default maximum scale. */
  public static final int DEFAULT_MAX_SCALE = 20;

  private static final Base2ExponentialHistogramOptions DEFAULT = builder().build();

  Base2ExponentialHistogramOptions() {}

  /** Returns the default {@link Base2ExponentialHistogramOptions}. */
  public static Base2ExponentialHistogramOptions getDefault() {
    return DEFAULT;
  }

  /** Returns a new {@link Builder} for {@link Base2ExponentialHistogramOptions}. */
  public static Builder builder() {
    return new AutoValue_Base2ExponentialHistogramOptions.Builder()
        .setMaxBuckets(DEFAULT_MAX_BUCKETS)
        .setMaxScale(DEFAULT_MAX_SCALE)
        .setRecordMinMax(true);
  }

  /** Returns a {@link Builder} initialized to the same property values as the current instance. */
  public abstract Builder toBuilder();

  /**
   * Returns the max number of positive and negative buckets. Defaults to {@value
   * #DEFAULT_MAX_BUCKETS}.
   */
  public abstract int getMaxBuckets();

  /** Returns the maximum and initial scale. Defaults to {@value #DEFAULT_MAX_SCALE}. */
  public abstract int getMaxScale();

  /** Returns whether min and max values should be recorded. Defaults to {@code true}. */
  public abstract boolean getRecordMinMax();

  /** Builder for {@link Base2ExponentialHistogramOptions}. */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the max number of positive buckets and negative buckets (max total buckets is 2 * {@code
     * maxBuckets} + 1 zero bucket).
     *
     * @param maxBuckets the maximum number of buckets
     */
    public abstract Builder setMaxBuckets(int maxBuckets);

    /**
     * Sets the maximum and initial scale. If measurements can't fit in a particular scale given the
     * {@code maxBuckets}, the scale is reduced until the measurements can be accommodated. Setting
     * maxScale may reduce the number of downscales. Additionally, the performance of computing
     * bucket index is improved when scale is {@code <= 0}.
     *
     * @param maxScale the maximum scale
     */
    public abstract Builder setMaxScale(int maxScale);

    /**
     * Sets whether min and max values should be recorded.
     *
     * @param recordMinMax whether to record min and max values
     */
    public abstract Builder setRecordMinMax(boolean recordMinMax);

    abstract Base2ExponentialHistogramOptions autoBuild();

    /**
     * Returns a new {@link Base2ExponentialHistogramOptions} with the configuration of this
     * builder.
     *
     * @throws IllegalArgumentException if {@code maxBuckets} is less than 2, or if {@code maxScale}
     *     is not between -10 and 20 (inclusive).
     */
    public Base2ExponentialHistogramOptions build() {
      Base2ExponentialHistogramOptions options = autoBuild();
      checkArgument(options.getMaxBuckets() >= 2, "maxBuckets must be >= 2");
      checkArgument(
          options.getMaxScale() >= -10 && options.getMaxScale() <= 20,
          "maxScale must be -10 <= x <= 20");
      return options;
    }
  }
}
