/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounter;
import io.opentelemetry.api.incubator.metrics.ExtendedLongCounterBuilder;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.ObservableLongCounter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SdkLongCounter extends AbstractInstrument implements ExtendedLongCounter {

  private static final Logger logger = Logger.getLogger(SdkLongCounter.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  private final MeterSharedState meterSharedState;
  private final WriteableMetricStorage storage;

  private SdkLongCounter(
      InstrumentDescriptor descriptor,
      MeterSharedState meterSharedState,
      WriteableMetricStorage storage) {
    super(descriptor);
    this.meterSharedState = meterSharedState;
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

  @Override
  public boolean enabled() {
    return meterSharedState.isMeterEnabled() && storage.isEnabled();
  }

  static final class SdkLongCounterBuilder implements ExtendedLongCounterBuilder {

    private final InstrumentBuilder builder;

    SdkLongCounterBuilder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name) {
      this.builder =
          new InstrumentBuilder(
              name,
              InstrumentType.COUNTER,
              InstrumentValueType.LONG,
              meterProviderSharedState,
              meterSharedState);
    }

    @Override
    public LongCounterBuilder setDescription(String description) {
      builder.setDescription(description);
      return this;
    }

    @Override
    public LongCounterBuilder setUnit(String unit) {
      builder.setUnit(unit);
      return this;
    }

    @Override
    public SdkLongCounter build() {
      return builder.buildSynchronousInstrument(SdkLongCounter::new);
    }

    @Override
    public DoubleCounterBuilder ofDoubles() {
      return builder.swapBuilder(SdkDoubleCounter.SdkDoubleCounterBuilder::new);
    }

    @Override
    public ObservableLongCounter buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      return builder.buildLongAsynchronousInstrument(InstrumentType.OBSERVABLE_COUNTER, callback);
    }

    @Override
    public ObservableLongMeasurement buildObserver() {
      return builder.buildObservableMeasurement(InstrumentType.OBSERVABLE_COUNTER);
    }

    @Override
    public ExtendedLongCounterBuilder setAttributesAdvice(List<AttributeKey<?>> attributes) {
      builder.setAdviceAttributes(attributes);
      return this;
    }

    @Override
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }
}
