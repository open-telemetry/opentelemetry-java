/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.instrument.BoundLongHistogram;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SdkLongHistogram extends AbstractInstrument implements LongHistogram {
  private static final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(SdkLongHistogram.class.getName()));
  private final WriteableMetricStorage storage;

  private SdkLongHistogram(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void record(long value, Attributes attributes, Context context) {
    if (value < 0) {
      logger.log(
          Level.WARNING,
          "Histograms can only record non-negative values. Instrument "
              + getDescriptor().getName()
              + " has recorded a negative value.");
      return;
    }
    storage.recordLong(value, attributes, context);
  }

  @Override
  public void record(long value, Attributes attributes) {
    record(value, attributes, Context.current());
  }

  @Override
  public void record(long value) {
    record(value, Attributes.empty());
  }

  BoundLongHistogram bind(Attributes attributes) {
    return new BoundInstrument(getDescriptor(), storage.bind(attributes), attributes);
  }

  static final class BoundInstrument implements BoundLongHistogram {
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
    public void record(long value, Context context) {
      if (value < 0) {
        logger.log(
            Level.WARNING,
            "Histograms can only record non-negative values. Instrument "
                + descriptor.getName()
                + " has recorded a negative value.");
        return;
      }
      handle.recordLong(value, attributes, context);
    }

    @Override
    public void record(long value) {
      record(value, Context.current());
    }

    @Override
    public void unbind() {
      handle.release();
    }
  }

  static final class Builder extends AbstractInstrumentBuilder<SdkLongHistogram.Builder>
      implements LongHistogramBuilder {

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
    public SdkLongHistogram build() {
      return buildSynchronousInstrument(
          InstrumentType.HISTOGRAM, InstrumentValueType.LONG, SdkLongHistogram::new);
    }
  }
}
