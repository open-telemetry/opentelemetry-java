/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
import java.util.concurrent.atomic.LongAdder;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
final class LongSumAggregator implements Aggregator<LongAccumulation> {
  static final LongSumAggregator INSTANCE = new LongSumAggregator();

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
