/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.RandomSupplier;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleSumAggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.LongSumAggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import java.util.function.Supplier;

/**
 * A sum aggregation configuration.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class SumAggregation implements Aggregation, AggregatorFactory {
  private static final SumAggregation INSTANCE = new SumAggregation();

  public static Aggregation getInstance() {
    return INSTANCE;
  }

  private SumAggregation() {}

  @Override
  @SuppressWarnings("unchecked")
  public <T extends PointData, U extends ExemplarData> Aggregator<T, U> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    switch (instrumentDescriptor.getValueType()) {
      case LONG:
        {
          Supplier<ExemplarReservoir<LongExemplarData>> reservoirFactory =
              () ->
                  ExemplarReservoir.filtered(
                      exemplarFilter,
                      ExemplarReservoir.longFixedSizeReservoir(
                          Clock.getDefault(),
                          Runtime.getRuntime().availableProcessors(),
                          RandomSupplier.platformDefault()));
          return (Aggregator<T, U>) new LongSumAggregator(instrumentDescriptor, reservoirFactory);
        }
      case DOUBLE:
        {
          Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirFactory =
              () ->
                  ExemplarReservoir.filtered(
                      exemplarFilter,
                      ExemplarReservoir.doubleFixedSizeReservoir(
                          Clock.getDefault(),
                          Runtime.getRuntime().availableProcessors(),
                          RandomSupplier.platformDefault()));
          return (Aggregator<T, U>) new DoubleSumAggregator(instrumentDescriptor, reservoirFactory);
        }
    }
    throw new IllegalArgumentException("Invalid instrument value type");
  }

  @Override
  public boolean isCompatibleWithInstrument(InstrumentDescriptor instrumentDescriptor) {
    switch (instrumentDescriptor.getType()) {
      case COUNTER:
      case OBSERVABLE_COUNTER:
      case UP_DOWN_COUNTER:
      case OBSERVABLE_UP_DOWN_COUNTER:
      case HISTOGRAM:
        return true;
      default:
        return false;
    }
  }

  @Override
  public String toString() {
    return "SumAggregation";
  }
}
