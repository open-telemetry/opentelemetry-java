/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.DoubleUpDownCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.api.metrics.ObservableLongUpDownCounter;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import java.util.function.Consumer;

final class SdkLongUpDownCounter extends AbstractInstrument implements ExtendedLongUpDownCounter {

  private final SdkMeter sdkMeter;
  private final WriteableMetricStorage storage;

  private SdkLongUpDownCounter(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor);
    this.sdkMeter = sdkMeter;
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

  @Override
  public boolean isEnabled() {
    return sdkMeter.isMeterEnabled() && storage.isEnabled();
  }

  static final class SdkLongUpDownCounterBuilder implements ExtendedLongUpDownCounterBuilder {

    private final InstrumentBuilder builder;

    SdkLongUpDownCounterBuilder(SdkMeter sdkMeter, String name) {
      this.builder =
          new InstrumentBuilder(
              name, InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG, sdkMeter);
    }

    @Override
    public LongUpDownCounterBuilder setDescription(String description) {
      builder.setDescription(description);
      return this;
    }

    @Override
    public LongUpDownCounterBuilder setUnit(String unit) {
      builder.setUnit(unit);
      return this;
    }

    @Override
    public LongUpDownCounter build() {
      return builder.buildSynchronousInstrument(SdkLongUpDownCounter::new);
    }

    @Override
    public DoubleUpDownCounterBuilder ofDoubles() {
      return builder.swapBuilder(SdkDoubleUpDownCounter.SdkDoubleUpDownCounterBuilder::new);
    }

    @Override
    public ObservableLongUpDownCounter buildWithCallback(
        Consumer<ObservableLongMeasurement> callback) {
      return builder.buildLongAsynchronousInstrument(
          InstrumentType.OBSERVABLE_UP_DOWN_COUNTER, callback);
    }

    @Override
    public ObservableLongMeasurement buildObserver() {
      return builder.buildObservableMeasurement(InstrumentType.OBSERVABLE_UP_DOWN_COUNTER);
    }

    @Override
    public ExtendedLongUpDownCounterBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }

    @Override
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }
}
