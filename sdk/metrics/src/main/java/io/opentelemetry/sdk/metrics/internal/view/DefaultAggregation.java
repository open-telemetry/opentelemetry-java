/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Aggregation that selects the specified default based on instrument.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DefaultAggregation implements AggregationExtension {

  private static final Aggregation INSTANCE = new DefaultAggregation(null);

  public static Aggregation getInstance() {
    return INSTANCE;
  }

  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(DefaultAggregation.class.getName()));

  @Nullable private final ExemplarReservoirFactory reservoirFactory;

  private DefaultAggregation(@Nullable ExemplarReservoirFactory reservoirFactory) {
    this.reservoirFactory = reservoirFactory;
  }

  private static AggregationExtension resolve(InstrumentDescriptor instrument, boolean withAdvice) {
    switch (instrument.getType()) {
      case COUNTER:
      case UP_DOWN_COUNTER:
      case OBSERVABLE_COUNTER:
      case OBSERVABLE_UP_DOWN_COUNTER:
        return (AggregationExtension) SumAggregation.getInstance();
      case HISTOGRAM:
        if (withAdvice && instrument.getAdvice().getExplicitBucketBoundaries() != null) {
          return (AggregationExtension)
              ExplicitBucketHistogramAggregation.create(
                  instrument.getAdvice().getExplicitBucketBoundaries());
        }
        return (AggregationExtension) ExplicitBucketHistogramAggregation.getDefault();
      case OBSERVABLE_GAUGE:
        return (AggregationExtension) LastValueAggregation.getInstance();
    }
    logger.log(Level.WARNING, "Unable to find default aggregation for instrument: " + instrument);
    return (AggregationExtension) DropAggregation.getInstance();
  }

  @Override
  public <T extends PointData, U extends ExemplarData> Aggregator<T, U> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    if (this.reservoirFactory != null) {
      return resolve(instrumentDescriptor, /* withAdvice= */ true)
          .setExemplarReservoirFactory(this.reservoirFactory)
          .createAggregator(instrumentDescriptor, exemplarFilter);
    }
    return resolve(instrumentDescriptor, /* withAdvice= */ true)
        .createAggregator(instrumentDescriptor, exemplarFilter);
  }

  @Override
  public boolean isCompatibleWithInstrument(InstrumentDescriptor instrumentDescriptor) {
    // This should always return true
    return resolve(instrumentDescriptor, /* withAdvice= */ false)
        .isCompatibleWithInstrument(instrumentDescriptor);
  }

  @Override
  public String toString() {
    return "DefaultAggregation";
  }

  @Override
  public AggregationExtension setExemplarReservoirFactory(
      ExemplarReservoirFactory reservoirFactory) {
    return new DefaultAggregation(reservoirFactory);
  }
}
