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
  private final InstrumentKey instrumentKey;

  LongCounterImpl(
      Accumulator accumulator,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      String name,
      String description,
      String unit) {
    this.accumulator = accumulator;
    InstrumentDescriptor instrumentDescriptor =
        InstrumentDescriptor.create(
            name, description, unit, InstrumentType.COUNTER, InstrumentValueType.LONG);
    this.instrumentKey = InstrumentKey.create(instrumentDescriptor, instrumentationLibraryInfo);
  }

  @Override
  public void add(long increment, Labels labels) {
    accumulator.recordLongAdd(instrumentKey, labels, increment);
  }

  @Override
  public void add(long increment) {
    add(increment, Labels.empty());
  }

  @Override
  public BoundLongCounter bind(Labels labels) {
    return new BoundLongCounterImpl(accumulator, instrumentKey, labels);
  }
}
