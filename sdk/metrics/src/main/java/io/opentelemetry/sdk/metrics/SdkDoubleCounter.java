/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleCounter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

class SdkDoubleCounter extends AbstractInstrument implements DoubleCounter {
  private static final Logger logger = Logger.getLogger(SdkDoubleCounter.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  final SdkMeter sdkMeter;
  final WriteableMetricStorage storage;

  SdkDoubleCounter(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor);
    this.sdkMeter = sdkMeter;
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

  static class SdkDoubleCounterBuilder implements DoubleCounterBuilder {

    final InstrumentBuilder builder;

    SdkDoubleCounterBuilder(
        SdkMeter sdkMeter,
        String name,
        String description,
        String unit,
        Advice.AdviceBuilder adviceBuilder) {
      this.builder =
          new InstrumentBuilder(name, InstrumentType.COUNTER, InstrumentValueType.DOUBLE, sdkMeter)
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
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }
}
