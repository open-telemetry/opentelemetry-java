/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.metrics.LongCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongCounterSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements LongCounter {

  private LongCounterSdk(InstrumentDescriptor descriptor, InstrumentProcessor instrumentProcessor) {
    super(descriptor, instrumentProcessor);
  }

  @Override
  public void add(long increment, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    try {
      boundInstrument.add(increment);
    } finally {
      boundInstrument.unbind();
    }
  }

  @Override
  public void add(long increment) {
    add(increment, Labels.empty());
  }

  @Override
  BoundInstrument newBinding(InstrumentProcessor instrumentProcessor) {
    return new BoundInstrument(instrumentProcessor);
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements LongCounter.BoundLongCounter {

    BoundInstrument(InstrumentProcessor instrumentProcessor) {
      super(instrumentProcessor.getAggregator());
    }

    @Override
    public void add(long increment) {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
      recordLong(increment);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<LongCounterSdk.Builder>
      implements LongCounter.Builder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        MeterSdk meterSdk) {
      super(name, meterProviderSharedState, meterSharedState, meterSdk);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public LongCounterSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.COUNTER, InstrumentValueType.LONG);
      return register(new LongCounterSdk(instrumentDescriptor, getBatcher(instrumentDescriptor)));
    }
  }
}
