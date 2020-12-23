/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongCounterSdk extends AbstractSynchronousInstrument implements LongCounter {

  private LongCounterSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void add(long increment, Labels labels) {
    Aggregator<?> aggregator = acquireHandle(labels);
    try {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
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
  public BoundLongCounter bind(Labels labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements LongCounter.BoundLongCounter {
    private final Aggregator<?> aggregator;

    BoundInstrument(Aggregator<?> aggregator) {
      this.aggregator = aggregator;
    }

    @Override
    public void add(long increment) {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
      aggregator.recordLong(increment);
    }

    @Override
    public void unbind() {
      aggregator.release();
    }
  }

  static final class Builder extends AbstractSynchronousInstrumentBuilder<Builder>
      implements LongCounter.Builder {

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
