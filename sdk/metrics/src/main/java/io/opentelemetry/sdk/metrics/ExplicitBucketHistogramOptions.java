/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import com.google.auto.value.AutoValue;
import java.util.List;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Options for configuring explicit bucket histogram aggregations.
 *
 * @see Aggregation#explicitBucketHistogram(ExplicitBucketHistogramOptions)
 * @since 1.60.0
 */
@AutoValue
@Immutable
public abstract class ExplicitBucketHistogramOptions {

  private static final ExplicitBucketHistogramOptions DEFAULT = builder().build();

  ExplicitBucketHistogramOptions() {}

  /** Returns the default {@link ExplicitBucketHistogramOptions}. */
  public static ExplicitBucketHistogramOptions getDefault() {
    return DEFAULT;
  }

  /** Returns a new {@link Builder} for {@link ExplicitBucketHistogramOptions}. */
  public static Builder builder() {
    return new AutoValue_ExplicitBucketHistogramOptions.Builder().setRecordMinMax(true);
  }

  /** Returns a {@link Builder} initialized to the same property values as the current instance. */
  public abstract Builder toBuilder();

  /**
   * Returns the bucket boundaries, or {@code null} if the default OTel bucket boundaries should be
   * used.
   */
  @Nullable
  public abstract List<Double> getBucketBoundaries();

  /** Returns whether min and max values should be recorded. Defaults to {@code true}. */
  public abstract boolean getRecordMinMax();

  /**
   * Builder for {@link ExplicitBucketHistogramOptions}.
   *
   * @since 1.60.0
   */
  @AutoValue.Builder
  public abstract static class Builder {

    Builder() {}

    /**
     * Sets the bucket boundaries. If not set, the default OTel bucket boundaries are used.
     *
     * @param bucketBoundaries a list of (inclusive) upper bounds, in order from lowest to highest
     */
    public abstract Builder setBucketBoundaries(@Nullable List<Double> bucketBoundaries);

    /**
     * Sets whether min and max values should be recorded.
     *
     * @param recordMinMax whether to record min and max values
     */
    public abstract Builder setRecordMinMax(boolean recordMinMax);

    /**
     * Returns a new {@link ExplicitBucketHistogramOptions} with the configuration of this builder.
     */
    public abstract ExplicitBucketHistogramOptions build();
  }
}
