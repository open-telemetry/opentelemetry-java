/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.sdk.metrics.DoubleValueRecorderSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleValueRecorderSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements DoubleValueRecorder {

  private DoubleValueRecorderSdk(
      InstrumentDescriptor descriptor, InstrumentAccumulator instrumentAccumulator) {
    super(descriptor, instrumentAccumulator);
  }

  @Override
  public void record(double value, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    boundInstrument.record(value);
    boundInstrument.unbind();
  }

  @Override
  public void record(double value) {
    record(value, Labels.empty());
  }

  @Override
  BoundInstrument newBinding(InstrumentAccumulator instrumentAccumulator) {
    return new BoundInstrument(instrumentAccumulator);
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements BoundDoubleValueRecorder {

    BoundInstrument(InstrumentAccumulator instrumentAccumulator) {
      super(instrumentAccumulator.getAggregator());
    }

    @Override
    public void record(double value) {
      recordDouble(value);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<DoubleValueRecorderSdk.Builder>
      implements DoubleValueRecorder.Builder {

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
    public DoubleValueRecorderSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.VALUE_RECORDER, InstrumentValueType.DOUBLE);
      return register(
          new DoubleValueRecorderSdk(instrumentDescriptor, getBatcher(instrumentDescriptor)));
    }
  }
}
