/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleCounterBuilder;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.instrument.BoundDoubleCounter;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SdkDoubleCounter extends AbstractInstrument implements DoubleCounter {
  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(SdkDoubleCounter.class.getName()));
  private final WriteableMetricStorage storage;

  private SdkDoubleCounter(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void add(double increment, Attributes attributes, Context context) {
    if (increment < 0) {
      logger.log(
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

  BoundDoubleCounter bind(Attributes attributes) {
    return new BoundInstrument(getDescriptor(), storage.bind(attributes), attributes);
  }

  static final class BoundInstrument implements BoundDoubleCounter {
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
    public void add(double increment, Context context) {
      if (increment < 0) {
        logger.log(
            Level.WARNING,
            "Counters can only increase. Instrument "
                + descriptor.getName()
                + " has recorded a negative value.");
        return;
      }
      handle.recordDouble(increment, attributes, context);
    }

    @Override
    public void add(double increment) {
      add(increment, Context.current());
    }

    @Override
    public void unbind() {
      handle.release();
    }
  }

  static final class Builder extends AbstractInstrumentBuilder<SdkDoubleCounter.Builder>
      implements DoubleCounterBuilder {

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
    public SdkDoubleCounter build() {
      return buildSynchronousInstrument(
          InstrumentType.COUNTER, InstrumentValueType.DOUBLE, SdkDoubleCounter::new);
    }

    @Override
    public void buildWithCallback(Consumer<ObservableDoubleMeasurement> callback) {
      registerDoubleAsynchronousInstrument(InstrumentType.OBSERVABLE_SUM, callback);
    }
  }
}
