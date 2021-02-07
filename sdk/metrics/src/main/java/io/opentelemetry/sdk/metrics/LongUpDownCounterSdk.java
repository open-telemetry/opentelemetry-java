/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.BoundLongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongUpDownCounterSdk extends AbstractSynchronousInstrument
    implements LongUpDownCounter {

  private LongUpDownCounterSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<?> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void add(long increment, Labels labels) {
    AggregatorHandle<?> aggregatorHandle = acquireHandle(labels);
    try {
      aggregatorHandle.recordLong(increment);
    } finally {
      aggregatorHandle.release();
    }
  }

  @Override
  public void add(long increment) {
    add(increment, Labels.empty());
  }

  @Override
  public BoundLongUpDownCounter bind(Labels labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements BoundLongUpDownCounter {
    private final AggregatorHandle<?> aggregatorHandle;

    BoundInstrument(AggregatorHandle<?> aggregatorHandle) {
      this.aggregatorHandle = aggregatorHandle;
    }

    @Override
    public void add(long increment) {
      aggregatorHandle.recordLong(increment);
    }

    @Override
    public void unbind() {
      aggregatorHandle.release();
    }
  }

  static final class Builder
      extends AbstractSynchronousInstrumentBuilder<LongUpDownCounterSdk.Builder>
      implements LongUpDownCounterBuilder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(
          name,
          InstrumentType.UP_DOWN_COUNTER,
          InstrumentValueType.LONG,
          meterProviderSharedState,
          meterSharedState);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public LongUpDownCounterSdk build() {
      return buildInstrument(LongUpDownCounterSdk::new);
    }
  }
}
