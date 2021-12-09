/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.instrument.BoundLongCounter;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SdkLongCounter extends AbstractInstrument implements LongCounter {
  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(SdkLongCounter.class.getName()));
  private final WriteableMetricStorage storage;

  private SdkLongCounter(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void add(long increment, Attributes attributes, Context context) {
    if (increment < 0) {
      logger.log(
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

  BoundLongCounter bind(Attributes attributes) {
    return new BoundInstrument(getDescriptor(), storage.bind(attributes), attributes);
  }

  static final class BoundInstrument implements BoundLongCounter {
    private final InstrumentDescriptor descriptor;
    private final BoundStorageHandle handle;
    private final Attributes attributes;

    BoundInstrument(
        InstrumentDescriptor descriptor, BoundStorageHandle handle, Attributes attributes) {
      this.descriptor = descriptor;
      this.handle = handle;
      this.attributes = attributes;
    }

    @Override
    public void add(long increment, Context context) {
      if (increment < 0) {
        logger.log(
            Level.WARNING,
            "Counters can only increase. Instrument "
                + descriptor.getName()
                + " has recorded a negative value.");
        return;
      }
      handle.recordLong(increment, attributes, context);
    }

    @Override
    public void add(long increment) {
      add(increment, Context.current());
    }

    @Override
    public void unbind() {
      handle.release();
    }
  }

  static final class Builder extends AbstractInstrumentBuilder<Builder>
      implements LongCounterBuilder {

    Builder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        String name) {
      this(meterProviderSharedState, meterSharedState, name, "", "1");
    }

    Builder(
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState sharedState,
        String name,
        String description,
        String unit) {
      super(meterProviderSharedState, sharedState, name, description, unit);
    }

    @Override
    protected Builder getThis() {
      return this;
    }

    @Override
    public SdkLongCounter build() {
      return buildSynchronousInstrument(
          InstrumentType.COUNTER, InstrumentValueType.LONG, SdkLongCounter::new);
    }

    @Override
    public DoubleCounterBuilder ofDoubles() {
      return swapBuilder(SdkDoubleCounter.Builder::new);
    }

    @Override
    public void buildWithCallback(Consumer<ObservableLongMeasurement> callback) {
      registerLongAsynchronousInstrument(InstrumentType.OBSERVABLE_SUM, callback);
    }
  }
}
