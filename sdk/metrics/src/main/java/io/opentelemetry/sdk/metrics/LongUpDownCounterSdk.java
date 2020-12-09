/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.sdk.metrics.LongUpDownCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongUpDownCounterSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements LongUpDownCounter {

  private LongUpDownCounterSdk(
      InstrumentDescriptor descriptor, InstrumentAccumulator instrumentAccumulator) {
    super(descriptor, instrumentAccumulator);
  }

  @Override
  public void add(long increment, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    boundInstrument.add(increment);
    boundInstrument.unbind();
  }

  @Override
  public void add(long increment) {
    add(increment, Labels.empty());
  }

  @Override
  BoundInstrument newBinding(InstrumentAccumulator instrumentAccumulator) {
    return new BoundInstrument(instrumentAccumulator);
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements BoundLongUpDownCounter {

    BoundInstrument(InstrumentAccumulator instrumentAccumulator) {
      super(instrumentAccumulator.getAggregator());
    }

    @Override
    public void add(long increment) {
      recordLong(increment);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<LongUpDownCounterSdk.Builder>
      implements LongUpDownCounter.Builder {

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
    public LongUpDownCounterSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG);
      return register(
          new LongUpDownCounterSdk(instrumentDescriptor, getBatcher(instrumentDescriptor)));
    }
  }
}
