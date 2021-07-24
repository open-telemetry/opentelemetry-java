/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundDoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogram;
import io.opentelemetry.api.metrics.DoubleHistogramBuilder;
import io.opentelemetry.api.metrics.LongHistogramBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleValueRecorderSdk extends AbstractSynchronousInstrument
    implements DoubleHistogram {

  private DoubleValueRecorderSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<?> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void record(double value, Attributes labels, Context context) {
    AggregatorHandle<?> aggregatorHandle = acquireHandle(labels);
    try {
      aggregatorHandle.recordDouble(value);
    } finally {
      aggregatorHandle.release();
    }
  }

  @Override
  public void record(double value, Attributes labels) {
    record(value, labels, Context.current());
  }

  @Override
  public void record(double value) {
    record(value, Attributes.empty());
  }

  @Override
  public BoundDoubleHistogram bind(Attributes labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements BoundDoubleHistogram {
    private final AggregatorHandle<?> aggregatorHandle;

    BoundInstrument(AggregatorHandle<?> aggregatorHandle) {
      this.aggregatorHandle = aggregatorHandle;
    }

    @Override
    public void record(double value, Context context) {
      aggregatorHandle.recordDouble(value);
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

  static final class Builder extends AbstractInstrumentBuilder<DoubleValueRecorderSdk.Builder>
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
    public DoubleValueRecorderSdk build() {
      return buildSynchronousInstrument(
          InstrumentType.VALUE_RECORDER, InstrumentValueType.DOUBLE, DoubleValueRecorderSdk::new);
    }

    @Override
    public LongHistogramBuilder ofLongs() {
      return swapBuilder(LongValueRecorderSdk.Builder::new);
    }
  }
}
