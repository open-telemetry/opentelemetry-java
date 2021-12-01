/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarFilter;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Aggregation that selects the specified default based on instrument. */
class DefaultAggregation extends Aggregation {

  static final Aggregation INSTANCE = new DefaultAggregation();

  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(DefaultAggregation.class.getName()));

  private DefaultAggregation() {}

  private static Aggregation resolve(InstrumentDescriptor instrument) {
    switch (instrument.getType()) {
      case COUNTER:
      case UP_DOWN_COUNTER:
      case OBSERVABLE_SUM:
      case OBSERVABLE_UP_DOWN_SUM:
        return SumAggregation.DEFAULT;
      case HISTOGRAM:
        return ExplicitBucketHistogramAggregation.DEFAULT;
      case OBSERVABLE_GAUGE:
        return LastValueAggregation.INSTANCE;
    }
    logger.log(Level.WARNING, "Unable to find default aggregation for instrument: " + instrument);
    return DropAggregation.INSTANCE;
  }

  @Override
  public <T> Aggregator<T> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    return resolve(instrumentDescriptor).createAggregator(instrumentDescriptor, exemplarFilter);
  }

  @Override
  public String toString() {
    return "DefaultAggregation";
  }
}
