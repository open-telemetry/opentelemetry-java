/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundLongHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongHistogram;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.internal.state.BoundStorageHandle;

final class LongValueRecorderSdk extends AbstractSynchronousInstrument implements LongHistogram {

  private LongValueRecorderSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<?> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void record(long value, Attributes attributes, Context context) {
    AggregatorHandle<?> aggregatorHandle = acquireHandle(attributes);
    try {
      aggregatorHandle.recordLong(value, attributes, context);
    } finally {
      aggregatorHandle.release();
    }
  }

  @Override
  public void record(long value, Attributes attributes) {
    record(value, attributes, Context.current());
  }

  @Override
  public void record(long value) {
    record(value, Attributes.empty());
  }

  @Override
  public BoundLongHistogram bind(Attributes attributes) {
    return new BoundInstrument(acquireHandle(attributes), attributes);
  }

  static final class BoundInstrument implements BoundLongHistogram {
    private final BoundStorageHandle handle;
    private final Attributes attributes;

    BoundInstrument(BoundStorageHandle handle, Attributes attributes) {
      this.handle = handle;
      this.attributes = attributes;
    }

    @Override
    public void record(long value, Context context) {
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

  static final class Builder extends AbstractInstrumentBuilder<LongValueRecorderSdk.Builder>
      implements LongHistogramBuilder {

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
    public LongValueRecorderSdk build() {
      return buildSynchronousInstrument(
          InstrumentType.VALUE_RECORDER, InstrumentValueType.LONG, LongValueRecorderSdk::new);
    }

    @Override
    public DoubleHistogramBuilder ofDoubles() {
      return swapBuilder(DoubleValueRecorderSdk.Builder::new);
    }
  }
}
