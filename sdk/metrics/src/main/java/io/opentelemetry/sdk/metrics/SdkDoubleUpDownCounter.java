/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableDoubleUpDownCounter;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;

final class SdkDoubleUpDownCounter extends AbstractInstrument implements DoubleUpDownCounter {

  private final WriteableMetricStorage storage;

  private SdkDoubleUpDownCounter(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void add(double increment, Attributes attributes, Context context) {
    storage.recordDouble(increment, attributes, context);
  }

  @Override
  public void add(double increment, Attributes attributes) {
    add(increment, attributes, Context.current());
  }

  @Override
  public void add(double increment) {
    add(increment, Attributes.empty());
  }

  static final class SdkDoubleUpDownCounterBuilder
      extends AbstractInstrumentBuilder<SdkDoubleUpDownCounterBuilder>
      implements DoubleUpDownCounterBuilder {

    SdkDoubleUpDownCounterBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState sharedState,
        String name,
        String description,
        String unit) {
      super(
          meterProviderSharedState,
          sharedState,
          InstrumentType.UP_DOWN_COUNTER,
          InstrumentValueType.DOUBLE,
          name,
          description,
          unit);
    }

    @Override
    protected SdkDoubleUpDownCounterBuilder getThis() {
      return this;
    }

    @Override
    public DoubleUpDownCounter build() {
      return buildSynchronousInstrument(SdkDoubleUpDownCounter::new);
    }

    @Override
    public ObservableDoubleUpDownCounter buildWithCallback(
        Consumer<ObservableDoubleMeasurement> callback) {
      return registerDoubleAsynchronousInstrument(
          InstrumentType.OBSERVABLE_UP_DOWN_COUNTER, callback);
    }

    @Override
    public ObservableDoubleMeasurement buildObserver() {
      return buildObservableMeasurement(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER);
    }
  }
}
