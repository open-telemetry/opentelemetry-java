/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleGauge;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;

class SdkDoubleGauge extends AbstractInstrument implements DoubleGauge {

  final SdkMeter sdkMeter;
  final WriteableMetricStorage storage;

  SdkDoubleGauge(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor);
    this.sdkMeter = sdkMeter;
    this.storage = storage;
  }

  @Override
  public void set(double value, Attributes attributes) {
    storage.recordDouble(value, attributes, Context.current());
  }

  @Override
  public void set(double value, Attributes attributes, Context context) {
    storage.recordDouble(value, attributes, context);
  }

  @Override
  public void set(double value) {
    set(value, Attributes.empty());
  }

  static class SdkDoubleGaugeBuilder implements DoubleGaugeBuilder {
    final InstrumentBuilder builder;

    SdkDoubleGaugeBuilder(SdkMeter sdkMeter, String name) {
      builder =
          new InstrumentBuilder(name, InstrumentType.GAUGE, InstrumentValueType.DOUBLE, sdkMeter);
    }

    @Override
    public DoubleGaugeBuilder setDescription(String description) {
      builder.setDescription(description);
      return this;
    }

    @Override
    public DoubleGaugeBuilder setUnit(String unit) {
      builder.setUnit(unit);
      return this;
    }

    @Override
    public SdkDoubleGauge build() {
      return builder.buildSynchronousInstrument(SdkDoubleGauge::new);
    }

    @Override
    public LongGaugeBuilder ofLongs() {
      return builder.swapBuilder(SdkLongGauge.SdkLongGaugeBuilder::new);
    }

    @Override
    public ObservableDoubleGauge buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
      return builder.buildDoubleAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
    }

    @Override
    public ObservableDoubleMeasurement buildObserver() {
      return builder.buildObservableMeasurement(InstrumentType.OBSERVABLE_GAUGE);
    }

    @Override
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }
}
