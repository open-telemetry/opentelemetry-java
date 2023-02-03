/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.ObservableLongCounter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SdkLongCounter extends AbstractInstrument implements LongCounter {

  private static final Logger logger = Logger.getLogger(SdkLongCounter.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final WriteableMetricStorage storage;

  private SdkLongCounter(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void add(long increment, Attributes attributes, Context context) {
    if (increment < 0) {
      throttlingLogger.log(
          Level.WARNING,
          "Counters can only increase. Instrument "
              + getDescriptor().getName()
              + " has recorded a negative value.");
      return;
    }
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

  static final class SdkLongCounterBuilder extends AbstractInstrumentBuilder<SdkLongCounterBuilder>
      implements LongCounterBuilder {

    SdkLongCounterBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name) {
      super(
          meterProviderSharedState,
          meterSharedState,
          InstrumentType.COUNTER,
          InstrumentValueType.LONG,
          name,
          "",
          DEFAULT_UNIT);
    }

    @Override
    protected SdkLongCounterBuilder getThis() {
      return this;
    }

    @Override
    public SdkLongCounter build() {
      return buildSynchronousInstrument(SdkLongCounter::new);
    }

    @Override
    public DoubleCounterBuilder ofDoubles() {
      return swapBuilder(SdkDoubleCounter.SdkDoubleCounterBuilder::new);
    }

    @Override
    public ObservableLongCounter buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      return registerLongAsynchronousInstrument(InstrumentType.OBSERVABLE_COUNTER, callback);
    }

    @Override
    public ObservableLongMeasurement buildObserver() {
      return buildObservableMeasurement(InstrumentType.OBSERVABLE_COUNTER);
    }
  }
}
