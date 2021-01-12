/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import javax.annotation.concurrent.Immutable;

/**
 * An AggregationConfiguration describes how an aggregation should be performed. It includes both an
 * {@code Aggregator} which implements what shape of aggregation is created (i.e. histogram, sum,
 * minMaxSumCount, etc), and a {@link AggregationTemporality} which describes whether aggregations
 * should be reset with every collection interval, or continue to accumulate across collection
 * intervals.
 */
@AutoValue
@Immutable
public abstract class AggregationConfiguration {

  /** Returns a new configuration with the provided options. */
  public static AggregationConfiguration create(
      AggregatorFactory aggregatorFactory, AggregationTemporality aggregationTemporality) {
    return new AutoValue_AggregationConfiguration(aggregatorFactory, aggregationTemporality);
  }

  /** Returns the {@link AggregatorFactory} that should be used for this View. */
  public abstract AggregatorFactory getAggregatorFactory();

  /**
   * Returns the {@link AggregationTemporality} that should be used for this View (delta vs.
   * cumulative).
   */
  public abstract AggregationTemporality getTemporality();
}
