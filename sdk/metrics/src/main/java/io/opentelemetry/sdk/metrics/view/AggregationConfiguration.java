/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.metrics.Instrument;
import javax.annotation.concurrent.Immutable;

/**
 * An AggregationConfiguration describes how an aggregation should be performed. It includes both an
 * {@link Aggregation} which implements what shape of aggregation is created (i.e. histogram, sum,
 * minMaxSumCount, etc), and a {@link AggregationConfiguration.Temporality} which describes whether
 * aggregations should be reset with every collection interval, or continue to accumulate across
 * collection intervals.
 */
@AutoValue
@Immutable
public abstract class AggregationConfiguration {

  /** Returns a new configuration with the provided options. */
  public static AggregationConfiguration create(Aggregation aggregation, Temporality temporality) {
    return new AutoValue_AggregationConfiguration(aggregation, temporality);
  }

  /** Which {@link Aggregation} should be used for this View. */
  public abstract Aggregation aggregation();

  /** Returns the {@link Temporality} that should be used for this View (delta vs. cumulative). */
  public abstract Temporality temporality();

  /** An enumeration which describes the time period over which metrics should be aggregated. */
  public enum Temporality {
    /** Metrics will be aggregated only over the most recent collection interval. */
    DELTA,
    /** Metrics will be aggregated over the lifetime of the associated {@link Instrument}. */
    CUMULATIVE
  }
}
