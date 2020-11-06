/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongCounter.BoundLongCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

class BoundLongCounterImpl implements BoundLongCounter {

  private final Accumulator accumulator;
  private final InstrumentDescriptor instrumentDescriptor;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;
  private final Labels labels;

  BoundLongCounterImpl(
      Accumulator accumulator,
      InstrumentDescriptor instrumentDescriptor,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      Labels labels) {
    this.accumulator = accumulator;
    this.instrumentDescriptor = instrumentDescriptor;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    this.labels = labels;
  }

  @Override
  public void add(long increment) {
    accumulator.recordLongAdd(instrumentationLibraryInfo, instrumentDescriptor, increment, labels);
  }

  @Override
  public void unbind() {
    // todo worry about this later
  }
}
