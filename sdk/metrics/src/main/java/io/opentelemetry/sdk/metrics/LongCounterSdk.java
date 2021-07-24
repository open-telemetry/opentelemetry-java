/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import java.util.function.Consumer;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
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
  public void add(long increment, Attributes labels, Context context) {
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
  public void add(long increment, Attributes attributes) {
    add(increment, attributes, Context.current());
  }

  @Override
  public void add(long increment) {
    add(increment, Attributes.empty());
  }

  @Override
  public BoundLongCounter bind(Attributes labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements BoundLongCounter {
    private final AggregatorHandle<?> aggregatorHandle;

    BoundInstrument(AggregatorHandle<?> aggregatorHandle) {
      this.aggregatorHandle = aggregatorHandle;
    }

    @Override
    public void add(long increment, Context context) {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
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

  static final class Builder extends AbstractInstrumentBuilder<Builder>
      implements LongCounterBuilder {

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
    public LongCounterSdk build() {
      return buildSynchronousInstrument(InstrumentType.COUNTER,
      InstrumentValueType.LONG, LongCounterSdk::new);
    }

    @Override
    public DoubleCounterBuilder ofDoubles() {
      return swapBuilder(DoubleCounterSdk.Builder::new);
    }

    @Override
    public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      // TODO Auto-generated method stub
      
    }
  }
}
