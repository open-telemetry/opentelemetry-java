/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.metrics.Instrument;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

@AutoValue
@Immutable
public abstract class AggregationConfiguration {

  public static AggregationConfiguration create(Aggregation aggregation, Temporality temporality) {
    return new AutoValue_AggregationConfiguration(aggregation, temporality);
  }

  /** Which {@link Aggregation} should be used for this View. */
  @Nullable
  public abstract Aggregation aggregation();

  /** What {@link Temporality} should be used for this View (delta vs. cumulative). */
  @Nullable
  public abstract Temporality temporality();

  /** An enumeration which describes the time period over which metrics should be aggregated. */
  public enum Temporality {
    /** Metrics will be aggregated only over the most recent collection interval. */
    DELTA,
    /** Metrics will be aggregated over the lifetime of the associated {@link Instrument}. */
    CUMULATIVE
  }
}
