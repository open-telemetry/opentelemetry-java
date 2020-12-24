/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleCounterSdk extends AbstractSynchronousInstrument implements DoubleCounter {

  private DoubleCounterSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<?> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void add(double increment, Labels labels) {
    Aggregator<?> aggregator = acquireHandle(labels);
    try {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
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
  public BoundDoubleCounter bind(Labels labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements DoubleCounter.BoundDoubleCounter {
    private final Aggregator<?> aggregator;

    BoundInstrument(Aggregator<?> aggregator) {
      this.aggregator = aggregator;
    }

    @Override
    public void add(double increment) {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
      aggregator.recordDouble(increment);
    }

    @Override
    public void unbind() {
      aggregator.release();
    }
  }

  static final class Builder extends AbstractSynchronousInstrumentBuilder<DoubleCounterSdk.Builder>
      implements DoubleCounter.Builder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(
          name,
          InstrumentType.COUNTER,
          InstrumentValueType.DOUBLE,
          meterProviderSharedState,
          meterSharedState);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public DoubleCounterSdk build() {
      return buildInstrument(DoubleCounterSdk::new);
    }
  }
}
