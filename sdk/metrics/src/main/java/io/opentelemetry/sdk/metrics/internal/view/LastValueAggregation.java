/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.internal.RandomSupplier;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.LongLastValueAggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;

/**
 * Last-value aggregation configuration.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LastValueAggregation implements Aggregation, AggregatorFactory {

  private static final Aggregation INSTANCE = new LastValueAggregation();

  public static Aggregation getInstance() {
    return INSTANCE;
  }

  private LastValueAggregation() {}

  @Override
  @SuppressWarnings("unchecked")
  public <T extends PointData> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor,
      ExemplarFilterInternal exemplarFilter,
      MemoryMode memoryMode) {

    ExemplarReservoirFactory reservoirFactory =
        ExemplarReservoirFactory.filtered(
            exemplarFilter,
            ExemplarReservoirFactory.fixedSizeReservoir(
                Clock.getDefault(),
                Runtime.getRuntime().availableProcessors(),
                RandomSupplier.platformDefault()));

    switch (instrumentDescriptor.getValueType()) {
      case LONG:
        return (Aggregator<T>) new LongLastValueAggregator(reservoirFactory, memoryMode);
      case DOUBLE:
        return (Aggregator<T>) new DoubleLastValueAggregator(reservoirFactory, memoryMode);
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }

  @Override
  public boolean isCompatibleWithInstrument(InstrumentDescriptor instrumentDescriptor) {
    InstrumentType instrumentType = instrumentDescriptor.getType();
    return instrumentType == InstrumentType.OBSERVABLE_GAUGE
        || instrumentType == InstrumentType.GAUGE;
  }

  @Override
  public String toString() {
    return "LastValueAggregation";
  }
}
