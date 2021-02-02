/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongCounterSdk extends AbstractSynchronousInstrument implements LongCounter {

  private LongCounterSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<?> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void add(long increment, Labels labels) {
    AggregatorHandle<?> aggregatorHandle = acquireHandle(labels);
    try {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
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
  public BoundLongCounter bind(Labels labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements BoundLongCounter {
    private final AggregatorHandle<?> aggregatorHandle;

    BoundInstrument(AggregatorHandle<?> aggregatorHandle) {
      this.aggregatorHandle = aggregatorHandle;
    }

    @Override
    public void add(long increment) {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
      aggregatorHandle.recordLong(increment);
    }

    @Override
    public void unbind() {
      aggregatorHandle.release();
    }
  }

  static final class Builder extends AbstractSynchronousInstrumentBuilder<Builder>
      implements LongCounterBuilder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(
          name,
          InstrumentType.COUNTER,
          InstrumentValueType.LONG,
          meterProviderSharedState,
          meterSharedState);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public LongCounterSdk build() {
      return buildInstrument(LongCounterSdk::new);
    }
  }
}
