/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilterInternal;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Aggregation that selects the specified default based on instrument.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DefaultAggregation implements Aggregation, AggregatorFactory {

  private static final Aggregation INSTANCE = new DefaultAggregation();

  public static Aggregation getInstance() {
    return INSTANCE;
  }

  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(DefaultAggregation.class.getName()));

  private DefaultAggregation() {}

  private static Aggregation resolve(InstrumentDescriptor instrument, boolean withAdvice) {
    switch (instrument.getType()) {
      case COUNTER:
      case UP_DOWN_COUNTER:
      case OBSERVABLE_COUNTER:
      case OBSERVABLE_UP_DOWN_COUNTER:
        return SumAggregation.getInstance();
      case HISTOGRAM:
        if (withAdvice && instrument.getAdvice().getExplicitBucketBoundaries() != null) {
          return ExplicitBucketHistogramAggregation.create(
              instrument.getAdvice().getExplicitBucketBoundaries());
        }
        return ExplicitBucketHistogramAggregation.getDefault();
      case OBSERVABLE_GAUGE:
      case GAUGE:
        return LastValueAggregation.getInstance();
    }
    logger.log(Level.WARNING, "Unable to find default aggregation for instrument: " + instrument);
    return DropAggregation.getInstance();
  }

  @Override
  public <T extends PointData> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor,
      ExemplarFilterInternal exemplarFilter,
      MemoryMode memoryMode) {
    return ((AggregatorFactory) resolve(instrumentDescriptor, /* withAdvice= */ true))
        .createAggregator(instrumentDescriptor, exemplarFilter, memoryMode);
  }

  @Override
  public boolean isCompatibleWithInstrument(InstrumentDescriptor instrumentDescriptor) {
    // This should always return true
    return ((AggregatorFactory) resolve(instrumentDescriptor, /* withAdvice= */ false))
        .isCompatibleWithInstrument(instrumentDescriptor);
  }

  @Override
  public String toString() {
    return "DefaultAggregation";
  }
}
