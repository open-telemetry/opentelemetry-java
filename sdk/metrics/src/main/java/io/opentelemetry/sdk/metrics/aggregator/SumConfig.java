/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.instrument.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.instrument.InstrumentType;
import javax.annotation.concurrent.Immutable;

/** Configuration for {@code Sum} metric production. */
@AutoValue
@Immutable
public abstract class SumConfig {
  /** Returns the name of the sum metric produced. */
  public abstract String getName();
  /** Returns the description of the sum metric, which can be used in documentation. */
  public abstract String getDescription();
  /**
   * The unit in which the metric value is reported. Follows the format described by
   * http://unitsofmeasure.org/ucum.html.
   */
  public abstract String getUnit();
  /** Returns true if the sum is monotonic (more: no negative measuremnts allowed). */
  public abstract boolean isMonotonic();
  /**
   * Returns the {@code AggregationTemporality} of this metric,
   *
   * <p>AggregationTemporality describes if the aggregator reports delta changes since last report
   * time, or cumulative changes since a fixed start time.
   *
   * @return the {@code AggregationTemporality} of this metric
   */
  public abstract AggregationTemporality getTemporality();
  /**
   * Returns the {@code AggregationTemporality} of measurements reproted to this metric.
   *
   * <p>CUMULATIVE: measurements are full sums.
   *
   * <p>DELTA: measurements should be added to existing sums.
   */
  public abstract AggregationTemporality getMeasurementTemporality();

  public static Builder builder() {
    return new AutoValue_SumConfig.Builder();
  }

  /** Returns the default sum aggregation configuration for a given instrument. */
  public static SumConfig buildDefaultFromInstrument(InstrumentDescriptor instrument) {
    // TODO: assert insturment type is one of the Sum aggregations...
    return builder()
        .setName(instrument.getName())
        .setDescription(instrument.getDescription())
        .setUnit(instrument.getUnit())
        .setMonotonic(
            (instrument.getType() == InstrumentType.COUNTER)
                || (instrument.getType() == InstrumentType.OBSERVABLE_SUM))
        // Default to cumulative foor all metrics.
        .setTemporality(AggregationTemporality.CUMULATIVE)
        // Synchronoous instruments report delta sums, asynchronous cumulative.
        .setMeasurementTemporality(
            instrument.getType().isSynchronous()
                ? AggregationTemporality.DELTA
                : AggregationTemporality.CUMULATIVE)
        .build();
  }

  /** Builder for {@link SumConfig}. */
  @AutoValue.Builder
  abstract static class Builder {
    abstract Builder setName(String name);

    abstract Builder setDescription(String name);

    abstract Builder setUnit(String unit);

    abstract Builder setMonotonic(boolean monotonic);

    abstract Builder setTemporality(AggregationTemporality temporality);

    abstract Builder setMeasurementTemporality(AggregationTemporality temporality);

    abstract SumConfig build();
  }
}
