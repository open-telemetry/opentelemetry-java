/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.DoubleAccumulation;
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
public final class DoubleLastValueAggregator implements Aggregator<DoubleAccumulation> {
  private static final DoubleLastValueAggregator INSTANCE = new DoubleLastValueAggregator();

  /**
   * Returns the instance of this {@link Aggregator}.
   *
   * @return the instance of this {@link Aggregator}.
   */
  public static DoubleLastValueAggregator getInstance() {
    return INSTANCE;
  }

  private DoubleLastValueAggregator() {}

  @Override
  public AggregatorHandle<DoubleAccumulation> createHandle() {
    return new Handle();
  }

  @Override
  public DoubleAccumulation accumulateDouble(double value) {
    return DoubleAccumulation.create(value);
  }

  static final class Handle extends AggregatorHandle<DoubleAccumulation> {
    @Nullable private static final Double DEFAULT_VALUE = null;
    private final AtomicReference<Double> current = new AtomicReference<>(DEFAULT_VALUE);

    private Handle() {}

    @Override
    protected DoubleAccumulation doAccumulateThenReset() {
      return DoubleAccumulation.create(this.current.getAndSet(DEFAULT_VALUE));
    }

    @Override
    protected void doRecordDouble(double value) {
      current.set(value);
    }
  }
}
