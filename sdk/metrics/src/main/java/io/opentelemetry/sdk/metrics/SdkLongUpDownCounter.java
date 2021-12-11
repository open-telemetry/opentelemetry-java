/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.instrument.BoundLongUpDownCounter;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;

final class SdkLongUpDownCounter extends AbstractInstrument implements LongUpDownCounter {
  private final WriteableMetricStorage storage;

  private SdkLongUpDownCounter(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void add(long increment, Attributes attributes, Context context) {
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

  BoundLongUpDownCounter bind(Attributes attributes) {
    return new BoundInstrument(storage.bind(attributes), attributes);
  }

  static final class BoundInstrument implements BoundLongUpDownCounter {
    private final BoundStorageHandle handle;
    private final Attributes attributes;

    BoundInstrument(BoundStorageHandle handle, Attributes attributes) {
      this.handle = handle;
      this.attributes = attributes;
    }

    @Override
    public void add(long increment, Context context) {
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

  static final class Builder extends AbstractInstrumentBuilder<SdkLongUpDownCounter.Builder>
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
          InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG, SdkLongUpDownCounter::new);
    }

    @Override
    public DoubleUpDownCounterBuilder ofDoubles() {
      return swapBuilder(SdkDoubleUpDownCounter.Builder::new);
    }

    @Override
    public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      registerLongAsynchronousInstrument(InstrumentType.OBSERVABLE_UP_DOWN_SUM, callback);
    }
  }
}
