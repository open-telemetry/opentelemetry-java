/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundDoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.function.Consumer;

final class DoubleUpDownCounterSdk extends AbstractSynchronousInstrument
    implements DoubleUpDownCounter {

  private DoubleUpDownCounterSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<?> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void add(double increment, Attributes labels, Context context) {
    AggregatorHandle<?> aggregatorHandle = acquireHandle(labels);
    try {
      aggregatorHandle.recordDouble(increment);
    } finally {
      aggregatorHandle.release();
    }
  }

  @Override
  public void add(double increment, Attributes attributes) {
    add(increment, attributes, Context.current());
  }

  @Override
  public void add(double increment) {
    add(increment, Attributes.empty());
  }

  @Override
  public BoundDoubleUpDownCounter bind(Attributes labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements BoundDoubleUpDownCounter {
    private final AggregatorHandle<?> aggregatorHandle;

    BoundInstrument(AggregatorHandle<?> aggregatorHandle) {
      this.aggregatorHandle = aggregatorHandle;
    }

    @Override
    public void add(double increment, Context context) {
      aggregatorHandle.recordDouble(increment);
    }

    @Override
    public void add(double increment) {
      add(increment, Context.current());
    }

    @Override
    public void unbind() {
      aggregatorHandle.release();
    }
  }

  static final class Builder extends AbstractInstrumentBuilder<DoubleUpDownCounterSdk.Builder>
      implements DoubleUpDownCounterBuilder {

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
    public DoubleUpDownCounter build() {
      return buildSynchronousInstrument(
          InstrumentType.COUNTER, InstrumentValueType.DOUBLE, DoubleUpDownCounterSdk::new);
    }

    @Override
    public LongUpDownCounterBuilder ofLongs() {
      return swapBuilder(LongUpDownCounterSdk.Builder::new);
    }

    @Override
    public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
      buildDoubleAsynchronousInstrument(
          InstrumentType.UP_DOWN_SUM_OBSERVER, callback, DoubleUpDownSumObserverSdk::new);
    }
  }
}
