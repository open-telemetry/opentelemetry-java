/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.sdk.metrics.DoubleCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleCounterSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements DoubleCounter {

  private DoubleCounterSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentAccumulator instrumentAccumulator) {
    super(descriptor, meterProviderSharedState, meterSharedState, instrumentAccumulator);
  }

  @Override
  public void add(double increment, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    try {
      boundInstrument.add(increment);
    } finally {
      boundInstrument.unbind();
    }
  }

  @Override
  public void add(double increment) {
    add(increment, Labels.empty());
  }

  @Override
  BoundInstrument newBinding(InstrumentAccumulator instrumentAccumulator) {
    return new BoundInstrument(instrumentAccumulator);
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements DoubleCounter.BoundDoubleCounter {

    BoundInstrument(InstrumentAccumulator instrumentAccumulator) {
      super(instrumentAccumulator.getAggregator());
    }

    @Override
    public void add(double increment) {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
      recordDouble(increment);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<DoubleCounterSdk.Builder>
      implements DoubleCounter.Builder {

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
    public DoubleCounterSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
      return register(
          new DoubleCounterSdk(
              instrumentDescriptor,
              getMeterProviderSharedState(),
              getMeterSharedState(),
              getBatcher(instrumentDescriptor)));
    }
  }
}
