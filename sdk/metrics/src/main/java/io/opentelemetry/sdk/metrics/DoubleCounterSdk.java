/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import java.util.function.Consumer;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundDoubleCounter;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleCounterSdk extends AbstractSynchronousInstrument implements DoubleCounter {

  private DoubleCounterSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<?> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void add(double increment, Attributes labels, Context context) {
    AggregatorHandle<?> aggregatorHandle = acquireHandle(labels);
    try {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
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
  public BoundDoubleCounter bind(Attributes labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements BoundDoubleCounter {
    private final AggregatorHandle<?> aggregatorHandle;

    BoundInstrument(AggregatorHandle<?> aggregatorHandle) {
      this.aggregatorHandle = aggregatorHandle;
    }

    @Override
    public void add(double increment, Context context) {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
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

  static final class Builder extends AbstractInstrumentBuilder<DoubleCounterSdk.Builder>
      implements DoubleCounterBuilder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
          this(meterProviderSharedState, meterSharedState, name, "", "1");
    }

    Builder(
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState sharedState,
      String name,
      String description,
      String unit
    ) {
      super(meterProviderSharedState, sharedState, name, description, unit);
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public DoubleCounterSdk build() {
      return buildSynchronousInstrument(InstrumentType.COUNTER,
      InstrumentValueType.DOUBLE,DoubleCounterSdk::new);
    }

    @Override
    public LongCounterBuilder ofLongs() {
      return swapBuilder(LongCounterSdk.Builder::new);
    }

    @Override
    public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
      // TODO: Async building
      
    }
  }
}
