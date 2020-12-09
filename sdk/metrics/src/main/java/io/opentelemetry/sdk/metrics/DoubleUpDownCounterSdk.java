/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.sdk.metrics.DoubleUpDownCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleUpDownCounterSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements DoubleUpDownCounter {

  private DoubleUpDownCounterSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      InstrumentAccumulator instrumentAccumulator) {
    super(descriptor, meterProviderSharedState, meterSharedState, instrumentAccumulator);
  }

  @Override
  public void add(double increment, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    boundInstrument.add(increment);
    boundInstrument.unbind();
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
      implements BoundDoubleUpDownCounter {

    BoundInstrument(InstrumentAccumulator instrumentAccumulator) {
      super(instrumentAccumulator.getAggregator());
    }

    @Override
    public void add(double increment) {
      recordDouble(increment);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<DoubleUpDownCounterSdk.Builder>
      implements DoubleUpDownCounter.Builder {

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
    public DoubleUpDownCounterSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.DOUBLE);
      return register(
          new DoubleUpDownCounterSdk(
              instrumentDescriptor,
              getMeterProviderSharedState(),
              getMeterSharedState(),
              getBatcher(instrumentDescriptor)));
    }
  }
}
