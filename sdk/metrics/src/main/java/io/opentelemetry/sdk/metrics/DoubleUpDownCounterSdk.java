/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.sdk.metrics.DoubleUpDownCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleUpDownCounterSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements DoubleUpDownCounter {

  private DoubleUpDownCounterSdk(
      InstrumentDescriptor descriptor,
      SynchronousInstrumentAccumulator<BoundInstrument> accumulator) {
    super(descriptor, accumulator);
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

  static final class BoundInstrument extends AbstractBoundInstrument
      implements BoundDoubleUpDownCounter {

    BoundInstrument(Aggregator aggregator) {
      super(aggregator);
    }

    @Override
    public void add(double increment) {
      recordDouble(increment);
    }
  }

  static final class Builder
      extends AbstractSynchronousInstrumentBuilder<DoubleUpDownCounterSdk.Builder>
      implements DoubleUpDownCounter.Builder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(
          name,
          InstrumentType.UP_DOWN_COUNTER,
          InstrumentValueType.DOUBLE,
          meterProviderSharedState,
          meterSharedState);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public DoubleUpDownCounterSdk build() {
      return buildInstrument(BoundInstrument::new, DoubleUpDownCounterSdk::new);
    }
  }
}
