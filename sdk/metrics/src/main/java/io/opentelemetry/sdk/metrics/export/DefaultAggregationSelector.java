/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

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
   * Return the default aggregation for the {@link InstrumentType}.
   *
   * <p>The default aggregation is used when an instrument does not match any views.
   */
  Aggregation getDefaultAggregation(InstrumentType instrumentType);
}
