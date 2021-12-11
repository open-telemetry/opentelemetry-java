/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.instrument.BoundDoubleHistogram;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;
import io.opentelemetry.sdk.metrics.internal.state.MeterProviderSharedState;
import io.opentelemetry.sdk.metrics.internal.state.MeterSharedState;
import io.opentelemetry.sdk.metrics.internal.state.WriteableMetricStorage;

final class SdkDoubleHistogram extends AbstractInstrument implements DoubleHistogram {
  private final WriteableMetricStorage storage;

  private SdkDoubleHistogram(InstrumentDescriptor descriptor, WriteableMetricStorage storage) {
    super(descriptor);
    this.storage = storage;
  }

  @Override
  public void record(double value, Attributes attributes, Context context) {
    storage.recordDouble(value, attributes, context);
  }

  @Override
  public void record(double value, Attributes attributes) {
    record(value, attributes, Context.current());
  }

  @Override
  public void record(double value) {
    record(value, Attributes.empty());
  }

  BoundDoubleHistogram bind(Attributes attributes) {
    return new BoundInstrument(storage.bind(attributes), attributes);
  }

  static final class BoundInstrument implements BoundDoubleHistogram {
    private final BoundStorageHandle aggregatorHandle;
    private final Attributes attributes;

    BoundInstrument(BoundStorageHandle handle, Attributes attributes) {
      this.aggregatorHandle = handle;
      this.attributes = attributes;
    }

    @Override
    public void record(double value, Context context) {
      aggregatorHandle.recordDouble(value, attributes, context);
    }

    @Override
    public void record(double value) {
      record(value, Context.current());
    }

    @Override
    public void unbind() {
      aggregatorHandle.release();
    }
  }

  static final class Builder extends AbstractInstrumentBuilder<SdkDoubleHistogram.Builder>
      implements DoubleHistogramBuilder {

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
    public SdkDoubleHistogram build() {
      return buildSynchronousInstrument(
          InstrumentType.HISTOGRAM, InstrumentValueType.DOUBLE, SdkDoubleHistogram::new);
    }

    @Override
    public LongHistogramBuilder ofLongs() {
      return swapBuilder(SdkLongHistogram.Builder::new);
    }
  }
}
