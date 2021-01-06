/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.accumulation.LongAccumulation;
import java.util.concurrent.atomic.LongAdder;

public final class LongSumAggregator implements Aggregator<LongAccumulation> {
  private static final LongSumAggregator INSTANCE = new LongSumAggregator();

  /**
   * Returns the instance of this {@link Aggregator}.
   *
   * @return the instance of this {@link Aggregator}.
   */
  public static Aggregator<LongAccumulation> getInstance() {
    return INSTANCE;
  }

  private LongSumAggregator() {}

  @Override
  public AggregatorHandle<LongAccumulation> createHandle() {
    return new Handle();
  }

  @Override
  public LongAccumulation accumulateLong(long value) {
    return LongAccumulation.create(value);
  }

  static final class Handle extends AggregatorHandle<LongAccumulation> {
    private final LongAdder current = new LongAdder();

    @Override
    protected LongAccumulation doAccumulateThenReset() {
      return LongAccumulation.create(this.current.sumThenReset());
    }

    @Override
    public void doRecordLong(long value) {
      current.add(value);
    }
  }
}
