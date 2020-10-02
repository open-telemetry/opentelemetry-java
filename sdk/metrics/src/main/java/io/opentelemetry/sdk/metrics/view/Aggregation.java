/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import javax.annotation.concurrent.Immutable;

/**
 * {@link Aggregation} is the process of combining a certain set of recorded measurements for a
 * given {@code Instrument} into the equivalent {@code MetricData}.
 */
@Immutable
public interface Aggregation {

  /**
   * Returns an {@code AggregationFactory} that can be used to produce the {@link
   * io.opentelemetry.sdk.metrics.aggregator.Aggregator} that needs to be used to aggregate all the
   * values to produce this {@code Aggregation}.
   *
   * @param instrumentValueType the type of recorded values for the {@code Instrument}.
   * @return the {@code AggregationFactory}.
   */
  AggregatorFactory getAggregatorFactory(InstrumentValueType instrumentValueType);

  /**
   * Returns the {@link MetricData.Type} that this {@code Aggregation} will produce.
   *
   * @param instrumentType the type of the {@code Instrument}.
   * @param instrumentValueType the type of recorded values for the {@code Instrument}.
   * @return the {@link MetricData.Type} that this {@code Aggregation} will produce.
   */
  MetricData.Type getDescriptorType(
      InstrumentType instrumentType, InstrumentValueType instrumentValueType);

  /**
   * Returns the unit that this {@code Aggregation} will produce.
   *
   * @param initialUnit the initial unit for the {@code Instrument}'s measurements.
   * @return the unit that this {@code Aggregation} will produce.
   */
  String getUnit(String initialUnit);

  /**
   * Returns {@code true} if this {@code Aggregation} can be applied to the given {@code
   * InstrumentType}.
   *
   * @param instrumentType the type of the {@code Instrument}.
   * @return {@code true} if this {@code Aggregation} can be applied to the given {@code
   *     InstrumentType}.
   */
  boolean availableForInstrument(InstrumentType instrumentType);
}
