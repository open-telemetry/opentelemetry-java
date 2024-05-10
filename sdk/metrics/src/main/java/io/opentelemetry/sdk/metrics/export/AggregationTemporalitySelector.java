/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import java.util.StringJoiner;

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

  /**
   * A common implementation of {@link AggregationTemporalitySelector} which reduces memory.
   *
   * <p>{@link AggregationTemporality#DELTA} is returned for {@link InstrumentType#COUNTER} and
   * {@link InstrumentType#HISTOGRAM}. {@link AggregationTemporality#CUMULATIVE} is returned for
   * {@link InstrumentType#UP_DOWN_COUNTER}, {@link InstrumentType#OBSERVABLE_UP_DOWN_COUNTER}, and
   * {@link InstrumentType#OBSERVABLE_COUNTER}.
   *
   * @since 1.28.0
   */
  static AggregationTemporalitySelector lowMemory() {
    return instrumentType -> {
      switch (instrumentType) {
        case UP_DOWN_COUNTER:
        case OBSERVABLE_UP_DOWN_COUNTER:
        case OBSERVABLE_COUNTER:
          return AggregationTemporality.CUMULATIVE;
        case COUNTER:
        case HISTOGRAM:
        default:
          return AggregationTemporality.DELTA;
      }
    };
  }

  /** Return the aggregation temporality for the {@link InstrumentType}. */
  AggregationTemporality getAggregationTemporality(InstrumentType instrumentType);

  /**
   * Returns a string representation of this selector, for using in {@link Object#toString()}
   * implementations.
   *
   * @since 1.38.0
   */
  static String asString(AggregationTemporalitySelector selector) {
    StringJoiner joiner = new StringJoiner(", ", "AggregationTemporalitySelector{", "}");
    for (InstrumentType type : InstrumentType.values()) {
      joiner.add(type.name() + "=" + selector.getAggregationTemporality(type).name());
    }
    return joiner.toString();
  }
}
