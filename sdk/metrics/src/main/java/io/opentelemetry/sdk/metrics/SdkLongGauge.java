/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.ExtendedLongGaugeBuilder;
import io.opentelemetry.api.metrics.LongGauge;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import java.util.function.Consumer;

final class SdkLongGauge extends AbstractInstrument implements LongGauge {

  private final WriteableMetricStorage storage;

  private SdkLongGauge(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void set(long value, Attributes attributes) {
    storage.recordLong(value, attributes, Context.current());
  }

  @Override
  public void set(long value, Attributes attributes, Context context) {
    storage.recordLong(value, attributes, context);
  }

  @Override
  public void set(long increment) {
    set(increment, Attributes.empty());
  }

  static final class SdkLongGaugeBuilder implements ExtendedLongGaugeBuilder {

    private final InstrumentBuilder builder;

    SdkLongGaugeBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState sharedState,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {

      builder =
          new InstrumentBuilder(
                  name,
                  InstrumentType.GAUGE,
                  InstrumentValueType.LONG,
                  meterProviderSharedState,
                  sharedState)
              .setDescription(description)
              .setUnit(unit)
              .setAdviceBuilder(adviceBuilder);
    }

    @Override
    public LongGaugeBuilder setDescription(String description) {
      builder.setDescription(description);
      return this;
    }

    @Override
    public LongGaugeBuilder setUnit(String unit) {
      builder.setUnit(unit);
      return this;
    }

    @Override
    public SdkLongGauge build() {
      return builder.buildSynchronousInstrument(SdkLongGauge::new);
    }

    @Override
    public ExtendedLongGaugeBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }

    @Override
    public ObservableLongGauge buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      return builder.buildLongAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
    }

    @Override
    public ObservableLongMeasurement buildObserver() {
      return builder.buildObservableMeasurement(InstrumentType.OBSERVABLE_GAUGE);
    }

    @Override
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }
}
