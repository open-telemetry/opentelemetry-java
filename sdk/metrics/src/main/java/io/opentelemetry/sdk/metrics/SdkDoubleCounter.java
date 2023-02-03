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
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SdkDoubleCounter extends AbstractInstrument implements DoubleCounter {
  private static final Logger logger = Logger.getLogger(SdkDoubleCounter.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final WriteableMetricStorage storage;

  private SdkDoubleCounter(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
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

  static final class SdkDoubleCounterBuilder
      extends AbstractInstrumentBuilder<SdkDoubleCounterBuilder> implements DoubleCounterBuilder {

    SdkDoubleCounterBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState sharedState,
        String name,
        String description,
        String unit) {
      super(
          meterProviderSharedState,
          sharedState,
          InstrumentType.COUNTER,
          InstrumentValueType.DOUBLE,
          name,
          description,
          unit);
    }

    @Override
    protected SdkDoubleCounterBuilder getThis() {
      return this;
    }

    @Override
    public SdkDoubleCounter build() {
      return buildSynchronousInstrument(SdkDoubleCounter::new);
    }

    @Override
    public ObservableDoubleCounter buildWithCallback(
        Consumer<ObservableDoubleMeasurement> callback) {
      return registerDoubleAsynchronousInstrument(InstrumentType.OBSERVABLE_COUNTER, callback);
    }

    @Override
    public ObservableDoubleMeasurement buildObserver() {
      return buildObservableMeasurement(InstrumentType.OBSERVABLE_COUNTER);
    }
  }
}
