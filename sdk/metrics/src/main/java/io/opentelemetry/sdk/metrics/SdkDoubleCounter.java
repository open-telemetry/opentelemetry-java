/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedDoubleCounterBuilder;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleCounter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SdkDoubleCounter extends AbstractInstrument implements ExtendedDoubleCounter {
  private static final Logger logger = Logger.getLogger(SdkDoubleCounter.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final MeterSharedState meterSharedState;
  private final WriteableMetricStorage storage;

  private SdkDoubleCounter(
      InstrumentDescriptor descriptor,
      MeterSharedState meterSharedState,
      WriteableMetricStorage storage) {
    super(descriptor);
    this.meterSharedState = meterSharedState;
    this.storage = storage;
  }

  @Override
  public void add(double increment, Attributes attributes, Context context) {
    if (increment < 0) {
      throttlingLogger.log(
          Level.WARNING,
          "Counters can only increase. Instrument "
              + getDescriptor().getName()
              + " has recorded a negative value.");
      return;
    }
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

  @Override
  public boolean isEnabled() {
    return meterSharedState.isMeterEnabled() && storage.isEnabled();
  }

  static final class SdkDoubleCounterBuilder implements ExtendedDoubleCounterBuilder {

    private final InstrumentBuilder builder;

    SdkDoubleCounterBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState sharedState,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {
      this.builder =
          new InstrumentBuilder(
                  name,
                  InstrumentType.COUNTER,
                  InstrumentValueType.DOUBLE,
                  meterProviderSharedState,
                  sharedState)
              .setUnit(unit)
              .setDescription(description)
              .setAdviceBuilder(adviceBuilder);
    }

    @Override
    public SdkDoubleCounter build() {
      return builder.buildSynchronousInstrument(SdkDoubleCounter::new);
    }

    @Override
    public DoubleCounterBuilder setDescription(String description) {
      builder.setDescription(description);
      return this;
    }

    @Override
    public DoubleCounterBuilder setUnit(String unit) {
      builder.setUnit(unit);
      return this;
    }

    @Override
    public ObservableDoubleCounter buildWithCallback(
        Consumer<ObservableDoubleMeasurement> callback) {
      return builder.buildDoubleAsynchronousInstrument(InstrumentType.OBSERVABLE_COUNTER, callback);
    }

    @Override
    public ObservableDoubleMeasurement buildObserver() {
      return builder.buildObservableMeasurement(InstrumentType.OBSERVABLE_COUNTER);
    }

    @Override
    public ExtendedDoubleCounterBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }

    @Override
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }
}
