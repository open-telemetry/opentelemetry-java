/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metricsv2;

import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

class LongSumAggregator implements LongAggregator<LongAccumulation> {
  private final LongAdder longAdder = new LongAdder();
  private final boolean keepCumulativeSums;
  private final AtomicLong startTime = new AtomicLong();

  LongSumAggregator(long startTime, boolean keepCumulativeSums) {
    this.startTime.set(startTime);
    this.keepCumulativeSums = keepCumulativeSums;
  }

  @Override
  public void record(long recording) {
    longAdder.add(recording);
  }

  @Override
  public LongAccumulation collect(Clock clock) {
    long value = keepCumulativeSums ? longAdder.sum() : longAdder.sumThenReset();
    if (!keepCumulativeSums) {
      startTime.set(clock.now());
    }
    return LongAccumulation.create(startTime.get(), value);
  }

  @Override
  public void merge(LongAccumulation accumulation) {
    record(accumulation.value());
  }
}
