/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.aggregation.Aggregation;
import io.opentelemetry.sdk.metrics.data.MetricData;
import javax.annotation.concurrent.Immutable;

/**
 * An AggregationConfiguration describes how an aggregation should be performed. It includes both an
 * {@link Aggregation} which implements what shape of aggregation is created (i.e. histogram, sum,
 * minMaxSumCount, etc), and a {@link MetricData.AggregationTemporality} which describes whether
 * aggregations should be reset with every collection interval, or continue to accumulate across
 * collection intervals.
 */
@AutoValue
@Immutable
public abstract class AggregationConfiguration {

  /** Returns a new configuration with the provided options. */
  public static AggregationConfiguration create(
      Aggregation aggregation, MetricData.AggregationTemporality aggregationTemporality) {
    return new AutoValue_AggregationConfiguration(aggregation, aggregationTemporality);
  }

  /** Returns the {@link Aggregation} that should be used for this View. */
  public abstract Aggregation aggregation();

  /**
   * Returns the {@link MetricData.AggregationTemporality} that should be used for this View (delta
   * vs. cumulative).
   */
  public abstract MetricData.AggregationTemporality temporality();
}
