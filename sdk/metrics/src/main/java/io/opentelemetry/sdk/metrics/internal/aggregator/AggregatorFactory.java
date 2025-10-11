/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;

/**
 * An internal interface for returning an Aggregator from an Aggregation.
 *
 * <p>This interface should be removed when adding support for custom aggregations to the metrics
 * SDK.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface AggregatorFactory {
  /**
   * Returns a new {@link Aggregator}.
   *
   * @param instrumentDescriptor the descriptor of the {@code Instrument} that will record
   *     measurements.
   * @param exemplarFilter the filter on which measurements should turn into exemplars
   * @param memoryMode The {@link MemoryMode} the aggregator will use
   * @return a new {@link Aggregator}. {@link Aggregator#drop()} indicates no measurements should be
   *     recorded.
   */
  <T extends PointData> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor,
      ExemplarFilter exemplarFilter,
      MemoryMode memoryMode);

  /**
   * Determine if the {@link Aggregator} produced by {@link #createAggregator(InstrumentDescriptor,
   * ExemplarFilter, MemoryMode)} is compatible with the {@code instrumentDescriptor}.
   */
  boolean isCompatibleWithInstrument(InstrumentDescriptor instrumentDescriptor);
}
