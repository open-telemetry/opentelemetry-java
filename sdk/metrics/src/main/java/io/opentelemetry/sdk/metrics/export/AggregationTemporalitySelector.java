/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;

/**
 * A functional interface that selects {@link AggregationTemporality} based on {@link
 * InstrumentType}.
 *
 * @since 1.14.0
 */
@FunctionalInterface
public interface AggregationTemporalitySelector {

  /**
   * A common implementation of {@link AggregationTemporalitySelector} which returns {@link
   * AggregationTemporality#CUMULATIVE} for all instruments.
   */
  static AggregationTemporalitySelector alwaysCumulative() {
    return instrumentType -> AggregationTemporality.CUMULATIVE;
  }

  /**
   * A common implementation of {@link AggregationTemporalitySelector} which indicates delta
   * preference.
   *
   * <p>{@link AggregationTemporality#DELTA} is returned for {@link InstrumentType#COUNTER}, {@link
   * InstrumentType#OBSERVABLE_COUNTER}, and {@link InstrumentType#HISTOGRAM}. {@link
   * AggregationTemporality#CUMULATIVE} is returned for {@link InstrumentType#UP_DOWN_COUNTER} and
   * {@link InstrumentType#OBSERVABLE_UP_DOWN_COUNTER}.
   */
  static AggregationTemporalitySelector deltaPreferred() {
    return instrumentType -> {
      switch (instrumentType) {
        case UP_DOWN_COUNTER:
        case OBSERVABLE_UP_DOWN_COUNTER:
          return AggregationTemporality.CUMULATIVE;
        case COUNTER:
        case OBSERVABLE_COUNTER:
        case HISTOGRAM:
        default:
          return AggregationTemporality.DELTA;
      }
    };
  }

  /** Return the aggregation temporality for the {@link InstrumentType}. */
  AggregationTemporality getAggregationTemporality(InstrumentType instrumentType);
}
