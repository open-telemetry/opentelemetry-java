/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongGaugeBuilder;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.extension.incubator.metrics.ExtendedLongGaugeBuilder;
import io.opentelemetry.extension.incubator.metrics.LongGauge;
import io.opentelemetry.extension.incubator.metrics.LongGaugeAdviceConfigurer;
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
  public void set(long increment, Attributes attributes) {
    storage.recordLong(increment, attributes, Context.root());
  }

  @Override
  public void set(long increment) {
    set(increment, Attributes.empty());
  }

  static final class SdkLongGaugeBuilder extends AbstractInstrumentBuilder<SdkLongGaugeBuilder>
      implements ExtendedLongGaugeBuilder, LongGaugeAdviceConfigurer {

    SdkLongGaugeBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState sharedState,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {
      super(
          meterProviderSharedState,
          sharedState,
          // TODO: use InstrumentType.GAUGE when available
          InstrumentType.OBSERVABLE_GAUGE,
          InstrumentValueType.LONG,
          name,
          description,
          unit,
          adviceBuilder);
    }

    @Override
    protected SdkLongGaugeBuilder getThis() {
      return this;
    }

    @Override
    public SdkLongGauge build() {
      return buildSynchronousInstrument(SdkLongGauge::new);
    }

    @Override
    public LongGaugeBuilder setAdvice(Consumer<LongGaugeAdviceConfigurer> adviceConsumer) {
      adviceConsumer.accept(this);
      return this;
    }

    @Override
    public LongGaugeAdviceConfigurer setAttributes(List<AttributeKey<?>> attributes) {
      adviceBuilder.setAttributes(attributes);
      return this;
    }

    @Override
    public ObservableLongGauge buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      // TODO: use InstrumentType.GAUGE when available
      return registerLongAsynchronousInstrument(InstrumentType.OBSERVABLE_GAUGE, callback);
    }

    @Override
    public ObservableLongMeasurement buildObserver() {
      // TODO: use InstrumentType.GAUGE when available
      return buildObservableMeasurement(InstrumentType.OBSERVABLE_GAUGE);
    }
  }
}
