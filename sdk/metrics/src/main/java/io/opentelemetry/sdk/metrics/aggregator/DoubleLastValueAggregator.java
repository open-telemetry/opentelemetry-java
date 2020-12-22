/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.Accumulation;
import io.opentelemetry.sdk.metrics.aggregation.DoubleAccumulation;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * Aggregator that aggregates recorded values by storing the last recorded value.
 *
 * <p>Limitation: The current implementation does not store a time when the value was recorded, so
 * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
 * problem because LastValueAggregator is currently only available for Observers which record all
 * values once.
 */
public final class DoubleLastValueAggregator extends Aggregator {

  @Nullable private static final Double DEFAULT_VALUE = null;
  private static final AggregatorFactory AGGREGATOR_FACTORY = DoubleLastValueAggregator::new;

  private final AtomicReference<Double> current = new AtomicReference<>(DEFAULT_VALUE);

  /**
   * Returns an {@link AggregatorFactory} that produces {@link DoubleLastValueAggregator} instances.
   *
   * @return an {@link AggregatorFactory} that produces {@link DoubleLastValueAggregator} instances.
   */
  public static AggregatorFactory getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  protected Accumulation doAccumulateThenReset() {
    return DoubleAccumulation.create(this.current.getAndSet(DEFAULT_VALUE));
  }

  @Override
  protected void doRecordDouble(double value) {
    current.set(value);
  }
}
