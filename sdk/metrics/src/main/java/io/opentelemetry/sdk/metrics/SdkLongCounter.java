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
import io.opentelemetry.sdk.common.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

class SdkLongCounter extends AbstractInstrument implements LongCounter {

  private static final Logger logger = Logger.getLogger(SdkLongCounter.class.getName());

  private final ThrottlingLogger throttlingLogger = new ThrottlingLogger(logger);
  final SdkMeter sdkMeter;
  final WriteableMetricStorage storage;

  SdkLongCounter(
      InstrumentDescriptor descriptor, SdkMeter sdkMeter, WriteableMetricStorage storage) {
    super(descriptor);
    this.sdkMeter = sdkMeter;
    this.storage = storage;
  }

  @Override
  public boolean isEnabled() {
    return sdkMeter.isMeterEnabled() && storage.isEnabled();
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
  public LongCounter bind(Attributes attributes) {
    return storage.cachedBind(attributes, BoundLongCounter::new);
  }

  static class SdkLongCounterBuilder implements LongCounterBuilder {

    final InstrumentBuilder builder;

    SdkLongCounterBuilder(SdkMeter sdkMeter, String name) {
      this.builder =
          new InstrumentBuilder(name, InstrumentType.COUNTER, InstrumentValueType.LONG, sdkMeter);
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
    public String toString() {
      return builder.toStringHelper(getClass().getSimpleName());
    }
  }

  private final class BoundLongCounter implements LongCounter {

    private final AggregatorHandle<?> op;
    private final Attributes boundAttributes;

    BoundLongCounter(AggregatorHandle<?> op, Attributes boundAttributes) {
      this.op = op;
      this.boundAttributes = boundAttributes;
    }

    @Override
    public void add(long value) {
      op.recordLong(value);
    }

    @Override
    public void add(long value, Attributes attributes) {
      SdkLongCounter.this.add(value, boundAttributes.toBuilder().putAll(attributes).build());
    }

    @Override
    public void add(long value, Attributes attributes, Context context) {
      SdkLongCounter.this.add(
          value, boundAttributes.toBuilder().putAll(attributes).build(), context);
    }

    @Override
    public LongCounter bind(Attributes attributes) {
      return SdkLongCounter.this.bind(boundAttributes.toBuilder().putAll(attributes).build());
    }

    @Override
    public boolean isEnabled() {
      return SdkLongCounter.this.isEnabled();
    }
  }
}
