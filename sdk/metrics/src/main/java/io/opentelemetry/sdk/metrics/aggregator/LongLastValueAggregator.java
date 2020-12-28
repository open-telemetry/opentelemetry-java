/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Aggregator that aggregates recorded values by storing the last recorded value.
 *
 * <p>Limitation: The current implementation does not store a time when the value was recorded, so
 * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
 * problem because LastValueAggregator is currently only available for Observers which record all
 * values once.
 */
@ThreadSafe
final class LongLastValueAggregator implements Aggregator<LongAccumulation> {
  @Nullable private static final Long DEFAULT_VALUE = null;
  static final LongLastValueAggregator INSTANCE = new LongLastValueAggregator();

  private LongLastValueAggregator() {}

  @Override
  public AggregatorHandle<LongAccumulation> createHandle() {
    return new Handle();
  }

  @Override
  public LongAccumulation accumulateLong(long value) {
    return LongAccumulation.create(value);
  }

  static final class Handle extends AggregatorHandle<LongAccumulation> {
    private final AtomicReference<Long> current = new AtomicReference<>(DEFAULT_VALUE);

    @Override
    protected LongAccumulation doAccumulateThenReset() {
      return LongAccumulation.create(this.current.getAndSet(DEFAULT_VALUE));
    }

    @Override
    protected void doRecordLong(long value) {
      current.set(value);
    }
  }
}
