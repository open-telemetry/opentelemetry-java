/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import java.util.concurrent.atomic.LongAdder;

class LongSumAggregator implements LongAggregator {
  private final LongAdder longAdder = new LongAdder();
  private final boolean keepCumulativeSums;

  LongSumAggregator(boolean keepCumulativeSums) {
    this.keepCumulativeSums = keepCumulativeSums;
  }

  @Override
  public void record(long recording) {
    longAdder.add(recording);
  }

  @Override
  public Accumulation collect() {
    long value = keepCumulativeSums ? longAdder.sum() : longAdder.sumThenReset();
    return LongAccumulation.create(value);
  }
}
