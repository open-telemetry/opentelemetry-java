/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;

/**
 * A functional interface that selects default {@link Aggregation} based on {@link InstrumentType}.
 *
 * @since 1.16.0
 */
@FunctionalInterface
public interface DefaultAggregationSelector {

  /**
   * The default implementation of {@link DefaultAggregationSelector} which returns the default
   * aggregation for each instrument.
   */
  static DefaultAggregationSelector getDefault() {
    return instrumentType -> Aggregation.defaultAggregation();
  }

  /**
   * Returns a default aggregation selector which returns the given {@code aggregation} for the
   * given {@code instrumentType}, and defers to this for other instrument types.
   *
   * <p>For example, the following produces a selector which drops histograms and uses the default
   * aggregation for other instruments:
   *
   * <pre>{@code
   * // DefaultAggregationSelector selector =
   * //   DefaultAggregationSelector.getDefault()
   * //     .with(InstrumentType.HISTOGRAM, Aggregation.drop());
   * }</pre>
   *
   * @since 1.16.0
   */
  default DefaultAggregationSelector with(InstrumentType instrumentType, Aggregation aggregation) {
    requireNonNull(instrumentType, "instrumentType");
    requireNonNull(aggregation, "aggregation");
    return instrumentType1 -> {
      if (instrumentType1 == instrumentType) {
        return aggregation;
      }
      return getDefaultAggregation(instrumentType1);
    };
  }

  /**
   * Return the default aggregation for the {@link InstrumentType}.
   *
   * <p>The default aggregation is used when an instrument does not match any views.
   */
  Aggregation getDefaultAggregation(InstrumentType instrumentType);
}
