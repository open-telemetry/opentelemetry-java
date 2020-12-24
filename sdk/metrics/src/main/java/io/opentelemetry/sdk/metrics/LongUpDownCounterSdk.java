/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
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
    Aggregator<?> aggregator = acquireHandle(labels);
    try {
      aggregator.recordLong(increment);
    } finally {
      aggregator.release();
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
    private final Aggregator<?> aggregator;

    BoundInstrument(Aggregator<?> aggregator) {
      this.aggregator = aggregator;
    }

    @Override
    public void add(long increment) {
      aggregator.recordLong(increment);
    }

    @Override
    public void unbind() {
      aggregator.release();
    }
  }

  static final class Builder
      extends AbstractSynchronousInstrumentBuilder<LongUpDownCounterSdk.Builder>
      implements LongUpDownCounter.Builder {

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
