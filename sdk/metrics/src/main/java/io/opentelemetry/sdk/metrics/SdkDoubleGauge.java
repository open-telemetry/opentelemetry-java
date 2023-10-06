/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleGauge;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.extension.incubator.metrics.DoubleGauge;
import io.opentelemetry.extension.incubator.metrics.ExtendedDoubleGaugeBuilder;
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

  static final class SdkDoubleGaugeBuilder extends AbstractInstrumentBuilder<SdkDoubleGaugeBuilder>
      implements ExtendedDoubleGaugeBuilder {

    SdkDoubleGaugeBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name) {
      super(
          meterProviderSharedState,
          meterSharedState,
          // TODO: use InstrumentType.GAUGE when available
          InstrumentType.OBSERVABLE_GAUGE,
          InstrumentValueType.DOUBLE,
          name,
          "",
          DEFAULT_UNIT);
    }

    @Override
    protected SdkDoubleGaugeBuilder getThis() {
      return this;
    }

    @Override
    public SdkDoubleGauge build() {
      return buildSynchronousInstrument(SdkDoubleGauge::new);
    }

    @Override
    public ExtendedDoubleGaugeBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      adviceBuilder.setAttributes(attributes);
      return this;
    }

    @Override
    public LongGaugeBuilder ofLongs() {
      return swapBuilder(SdkLongGauge.SdkLongGaugeBuilder::new);
    }

    @Override
    public ObservableDoubleGauge buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
      // TODO: use InstrumentType.GAUGE when available
      return registerDoubleAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
    }

    @Override
    public ObservableDoubleMeasurement buildObserver() {
      // TODO: use InstrumentType.GAUGE when available
      return buildObservableMeasurement(InstrumentType.OBSERVABLE_GAUGE);
    }
  }
}
