/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import java.util.concurrent.atomic.LongAdder;

class LongSumAggregator implements LongAggregator {
  private final LongAdder longAdder = new LongAdder();

  @Override
  public void record(long recording) {
    longAdder.add(recording);
  }

  @Override
  public Accumulation collect() {
    // todo: reset if delta-style aggregation
    return LongAccumulation.create(longAdder.sum());
  }
}
