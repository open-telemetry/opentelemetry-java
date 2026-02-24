/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

/**
 * Options for configuring histogram aggregations.
 *
 * @see Aggregation#explicitBucketHistogram(java.util.List, HistogramOptions)
 * @see Aggregation#base2ExponentialBucketHistogram(int, int, HistogramOptions)
 */
public final class HistogramOptions {

  private final boolean recordMinMax;

  private HistogramOptions(Builder builder) {
    this.recordMinMax = builder.recordMinMax;
  }

  /** Returns a new {@link Builder} for {@link HistogramOptions}. */
  public static Builder builder() {
    return new Builder();
  }

  /** Returns whether min and max values should be recorded. Defaults to {@code true}. */
  public boolean isRecordMinMax() {
    return recordMinMax;
  }

  @Override
  public String toString() {
    return "HistogramOptions{recordMinMax=" + recordMinMax + "}";
  }

  /** Builder for {@link HistogramOptions}. */
  public static final class Builder {

    private boolean recordMinMax = true;

    private Builder() {}

    /**
     * Sets whether min and max values should be recorded.
     *
     * @param recordMinMax whether to record min and max values
     */
    public Builder setRecordMinMax(boolean recordMinMax) {
      this.recordMinMax = recordMinMax;
      return this;
    }

    /** Returns a new {@link HistogramOptions} with the configuration of this builder. */
    public HistogramOptions build() {
      return new HistogramOptions(this);
    }
  }
}
