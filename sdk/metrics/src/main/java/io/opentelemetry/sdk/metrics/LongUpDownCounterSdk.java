/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundLongUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.function.Consumer;

final class LongUpDownCounterSdk extends AbstractSynchronousInstrument
    implements LongUpDownCounter {

  private LongUpDownCounterSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<?> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void add(long increment, Attributes labels, Context context) {
    AggregatorHandle<?> aggregatorHandle = acquireHandle(labels);
    try {
      aggregatorHandle.recordLong(increment);
    } finally {
      aggregatorHandle.release();
    }
  }

  @Override
  public void add(long increment, Attributes attributes) {
    add(increment, attributes, Context.current());
  }

  @Override
  public void add(long increment) {
    add(increment, Attributes.empty());
  }

  @Override
  public BoundLongUpDownCounter bind(Attributes labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements BoundLongUpDownCounter {
    private final AggregatorHandle<?> aggregatorHandle;

    BoundInstrument(AggregatorHandle<?> aggregatorHandle) {
      this.aggregatorHandle = aggregatorHandle;
    }

    @Override
    public void add(long increment, Context context) {
      aggregatorHandle.recordLong(increment);
    }

    @Override
    public void add(long increment) {
      add(increment, Context.current());
    }

    @Override
    public void unbind() {
      aggregatorHandle.release();
    }
  }

  static final class Builder extends AbstractInstrumentBuilder<LongUpDownCounterSdk.Builder>
      implements LongUpDownCounterBuilder {

    Builder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name) {
      this(meterProviderSharedState, meterSharedState, name, "", "1");
    }

    Builder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState sharedState,
        String name,
        String description,
        String unit) {
      super(meterProviderSharedState, sharedState, name, description, unit);
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public LongUpDownCounter build() {
      return buildSynchronousInstrument(
          InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG, LongUpDownCounterSdk::new);
    }

    @Override
    public DoubleUpDownCounterBuilder ofDoubles() {
      return swapBuilder(DoubleUpDownCounterSdk.Builder::new);
    }

    @Override
    public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      buildLongAsynchronousInstrument(
          InstrumentType.UP_DOWN_SUM_OBSERVER, callback, LongUpDownSumObserverSdk::new);
    }
  }
}
