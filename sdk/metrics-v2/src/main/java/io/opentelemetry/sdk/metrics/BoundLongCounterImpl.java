/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongCounter.BoundLongCounter;

class BoundLongCounterImpl implements BoundLongCounter {

  private final Accumulator accumulator;
  private final InstrumentKey instrumentKey;
  private final Labels labels;

  BoundLongCounterImpl(Accumulator accumulator, InstrumentKey instrumentKey, Labels labels) {
    this.accumulator = accumulator;
    this.instrumentKey = instrumentKey;
    this.labels = labels;
  }

  @Override
  public void add(long increment) {
    accumulator.recordLongAdd(instrumentKey, labels, increment);
  }

  @Override
  public void unbind() {
    // todo worry about this later
  }
}
