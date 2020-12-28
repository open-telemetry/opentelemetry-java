/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.sdk.metrics.aggregation.LongAccumulation;
import java.util.concurrent.atomic.LongAdder;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
final class CountAggregator implements Aggregator<LongAccumulation> {
  static final CountAggregator INSTANCE = new CountAggregator();

  private CountAggregator() {}

  @Override
  public AggregatorHandle<LongAccumulation> createHandle() {
    return new Handle();
  }

  @Override
  public LongAccumulation accumulateDouble(double value) {
    return LongAccumulation.create(1);
  }

  @Override
  public LongAccumulation accumulateLong(long value) {
    return LongAccumulation.create(1);
  }

  static final class Handle extends AggregatorHandle<LongAccumulation> {
    private final LongAdder current = new LongAdder();

    private Handle() {}

    @Override
    protected void doRecordLong(long value) {
      current.add(1);
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(1);
    }

    @Override
    protected LongAccumulation doAccumulateThenReset() {
      return LongAccumulation.create(current.sumThenReset());
    }
  }
}
