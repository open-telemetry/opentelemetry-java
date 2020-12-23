/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleUpDownCounterSdk extends AbstractSynchronousInstrument
    implements DoubleUpDownCounter {

  private DoubleUpDownCounterSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<?> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void add(double increment, Labels labels) {
    Aggregator<?> aggregator = acquireHandle(labels);
    try {
      aggregator.recordDouble(increment);
    } finally {
      aggregator.release();
    }
  }

  @Override
  public void add(double increment) {
    add(increment, Labels.empty());
  }

  @Override
  public BoundDoubleUpDownCounter bind(Labels labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements BoundDoubleUpDownCounter {
    private final Aggregator<?> aggregator;

    BoundInstrument(Aggregator<?> aggregator) {
      this.aggregator = aggregator;
    }

    @Override
    public void add(double increment) {
      aggregator.recordDouble(increment);
    }

    @Override
    public void unbind() {
      aggregator.release();
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
      return buildInstrument(DoubleUpDownCounterSdk::new);
    }
  }
}
