/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.view;

import static io.opentelemetry.api.internal.Utils.checkArgument;

import io.opentelemetry.sdk.common.Clock;
import io.opentelemetry.sdk.internal.RandomSupplier;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.internal.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.internal.aggregator.DoubleExponentialHistogramAggregator;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.DoubleExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;

/**
 * Exponential bucket histogram aggregation configuration.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ExponentialHistogramAggregation implements Aggregation, AggregatorFactory {

  private static final Aggregation DEFAULT = new ExponentialHistogramAggregation(20, 320);

  public static Aggregation getDefault() {
    return DEFAULT;
  }

  public static Aggregation create(int scale, int maxBuckets) {
    checkArgument(maxBuckets >= 0, "maxBuckets must be >= 0");
    return new ExponentialHistogramAggregation(scale, maxBuckets);
  }

  private final int startingScale;
  private final int maxBuckets;

  private ExponentialHistogramAggregation(int startingScale, int maxBuckets) {
    this.startingScale = startingScale;
    this.maxBuckets = maxBuckets;
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T, U extends ExemplarData> Aggregator<T, U> createAggregator(
      InstrumentDescriptor instrumentDescriptor, ExemplarFilter exemplarFilter) {
    return (Aggregator<T, U>)
        new DoubleExponentialHistogramAggregator(
            () ->
                DoubleExemplarReservoir.filtered(
                    exemplarFilter,
                    DoubleExemplarReservoir.fixedSizeReservoir(
                        Clock.getDefault(),
                        Runtime.getRuntime().availableProcessors(),
                        RandomSupplier.platformDefault())),
            startingScale,
            maxBuckets);
  }

  @Override
  public boolean isCompatibleWithInstrument(InstrumentDescriptor instrumentDescriptor) {
    switch (instrumentDescriptor.getType()) {
      case COUNTER:
      case HISTOGRAM:
        return true;
      default:
        return false;
    }
  }

  @Override
  public String toString() {
    return "ExponentialHistogramAggregation{startingScale="
        + startingScale
        + ",maxBuckets="
        + maxBuckets
        + "}";
  }
}
