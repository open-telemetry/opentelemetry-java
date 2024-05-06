/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.DoubleGauge;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleGaugeBuilder;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import java.util.function.Consumer;

final class SdkDoubleGauge extends AbstractInstrument implements DoubleGauge {

  private final WriteableMetricStorage storage;

  private SdkDoubleGauge(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void set(double increment, Attributes attributes) {
    storage.recordDouble(increment, attributes, Context.root());
  }

  @Override
  public void set(double increment) {
    set(increment, Attributes.empty());
  }

  static final class SdkDoubleGaugeBuilder implements ExtendedDoubleGaugeBuilder {
    private final InstrumentBuilder builder;

    SdkDoubleGaugeBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name) {

      // TODO: use InstrumentType.GAUGE when available
      builder =
          new InstrumentBuilder(
              name,
              InstrumentType.OBSERVABLE_GAUGE,
              InstrumentValueType.DOUBLE,
              meterProviderSharedState,
              meterSharedState);
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
    public ExtendedDoubleGaugeBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }

    @Override
    public LongGaugeBuilder ofLongs() {
      return builder.swapBuilder(SdkLongGauge.SdkLongGaugeBuilder::new);
    }

    @Override
    public ObservableDoubleGauge buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
      // TODO: use InstrumentType.GAUGE when available
      return builder.buildDoubleAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
    }

    @Override
    public ObservableDoubleMeasurement buildObserver() {
      // TODO: use InstrumentType.GAUGE when available
      return builder.buildObservableMeasurement(InstrumentType.OBSERVABLE_GAUGE);
    }

    @Override
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }
}
