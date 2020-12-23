/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
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
public final class LongLastValueAggregator extends Aggregator<LongAccumulation> {
  @Nullable private static final Long DEFAULT_VALUE = null;
  private static final AggregatorFactory<LongAccumulation> AGGREGATOR_FACTORY =
      new AggregatorFactory<LongAccumulation>() {
        @Override
        public Aggregator<LongAccumulation> getAggregator() {
          return new LongLastValueAggregator();
        }

        @Override
        public LongAccumulation accumulateLong(long value) {
          return LongAccumulation.create(value);
        }
      };

  private final AtomicReference<Long> current = new AtomicReference<>(DEFAULT_VALUE);

  private LongLastValueAggregator() {}

  /**
   * Returns an {@link AggregatorFactory} that produces {@link LongLastValueAggregator} instances.
   *
   * @return an {@link AggregatorFactory} that produces {@link LongLastValueAggregator} instances.
   */
  public static AggregatorFactory<LongAccumulation> getFactory() {
    return AGGREGATOR_FACTORY;
  }

  @Override
  protected LongAccumulation doAccumulateThenReset() {
    return LongAccumulation.create(this.current.getAndSet(DEFAULT_VALUE));
  }

  @Override
  protected void doRecordLong(long value) {
    current.set(value);
  }
}
