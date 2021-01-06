/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.accumulation.DoubleAccumulation;
import java.util.concurrent.atomic.DoubleAdder;

public final class DoubleSumAggregator implements Aggregator<DoubleAccumulation> {
  private static final DoubleSumAggregator INSTANCE = new DoubleSumAggregator();

  /**
   * Returns the instance of this {@link Aggregator}.
   *
   * @return the instance of this {@link Aggregator}.
   */
  public static Aggregator<DoubleAccumulation> getInstance() {
    return INSTANCE;
  }

  private DoubleSumAggregator() {}

  @Override
  public AggregatorHandle<DoubleAccumulation> createHandle() {
    return new Handle();
  }

  @Override
  public DoubleAccumulation accumulateDouble(double value) {
    return DoubleAccumulation.create(value);
  }

  static final class Handle extends AggregatorHandle<DoubleAccumulation> {
    private final DoubleAdder current = new DoubleAdder();

    @Override
    protected DoubleAccumulation doAccumulateThenReset() {
      return DoubleAccumulation.create(this.current.sumThenReset());
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(value);
    }
  }
}
