/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;

final class LongCounterSdk extends AbstractInstrument implements LongCounter {
  private final WriteableMetricStorage storage;

  private LongCounterSdk(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void add(long increment, Attributes attributes, Context context) {
    if (increment < 0) {
      throw new IllegalArgumentException("Counters can only increase");
    }
    storage.recordLong(increment, attributes, context);
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
  public BoundLongCounter bind(Attributes attributes) {
    return new BoundInstrument(storage.bind(attributes), attributes);
  }

  static final class BoundInstrument implements BoundLongCounter {
    private final BoundStorageHandle handle;
    private final Attributes attributes;

    BoundInstrument(BoundStorageHandle handle, Attributes attributes) {
      this.handle = handle;
      this.attributes = attributes;
    }

    @Override
    public void add(long increment, Context context) {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
      handle.recordLong(increment, attributes, context);
    }

    @Override
    public void add(long increment) {
      add(increment, Context.current());
    }

    @Override
    public void unbind() {
      handle.release();
    }
  }

  static final class Builder extends AbstractInstrumentBuilder<Builder>
      implements LongCounterBuilder {

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
    public LongCounterSdk build() {
      return buildSynchronousInstrument(
          InstrumentType.COUNTER, InstrumentValueType.LONG, LongCounterSdk::new);
    }

    @Override
    public DoubleCounterBuilder ofDoubles() {
      return swapBuilder(DoubleCounterSdk.Builder::new);
    }

    @Override
    public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      registerLongAsynchronousInstrument(InstrumentType.OBSERVABLE_SUM, callback);
    }
  }
}
