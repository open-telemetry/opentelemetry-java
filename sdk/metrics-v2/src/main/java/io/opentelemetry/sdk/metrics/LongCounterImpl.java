/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;

class LongCounterImpl implements LongCounter {

  private final Accumulator accumulator;
  private final InstrumentDescriptor instrumentDescriptor;
  // todo: should the library info be folded into the descriptor?
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  LongCounterImpl(
      Accumulator accumulator,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit) {
    this.accumulator = accumulator;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
    this.instrumentDescriptor =
        InstrumentDescriptor.create(
            name, description, unit, InstrumentType.COUNTER, InstrumentValueType.LONG);
  }

  @Override
  public void add(long increment, Labels labels) {
    accumulator.recordLongAdd(instrumentationLibraryInfo, instrumentDescriptor, increment, labels);
  }

  @Override
  public void add(long increment) {
    add(increment, Labels.empty());
  }

  @Override
  public BoundLongCounter bind(Labels labels) {
    return new BoundLongCounterImpl(
        accumulator, instrumentDescriptor, instrumentationLibraryInfo, labels);
  }
}
