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
import io.opentelemetry.api.metrics.ObservableLongUpDownCounter;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
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

  static final class SdkLongUpDownCounterBuilder
      extends AbstractInstrumentBuilder<SdkLongUpDownCounterBuilder>
      implements LongUpDownCounterBuilder {

    SdkLongUpDownCounterBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name) {
      super(
          meterProviderSharedState,
          meterSharedState,
          InstrumentType.UP_DOWN_COUNTER,
          InstrumentValueType.LONG,
          name,
          "",
          DEFAULT_UNIT);
    }

    @Override
    protected SdkLongUpDownCounterBuilder getThis() {
      return this;
    }

    @Override
    public LongUpDownCounter build() {
      return buildSynchronousInstrument(SdkLongUpDownCounter::new);
    }

    @Override
    public DoubleUpDownCounterBuilder ofDoubles() {
      return swapBuilder(SdkDoubleUpDownCounter.SdkDoubleUpDownCounterBuilder::new);
    }

    @Override
    public ObservableLongUpDownCounter buildWithCallback(
        Consumer<ObservableLongMeasurement> callback) {
      return registerLongAsynchronousInstrument(
          InstrumentType.OBSERVABLE_UP_DOWN_COUNTER, callback);
    }

    @Override
    public ObservableLongMeasurement buildObserver() {
      return buildObservableMeasurement(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER);
    }
  }
}
