/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.BoundDoubleValueRecorder;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.DoubleValueRecorderBuilder;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorHandle;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleValueRecorderSdk extends AbstractSynchronousInstrument
    implements DoubleValueRecorder {

  private DoubleValueRecorderSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator<?> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void record(double value, Labels labels) {
    AggregatorHandle<?> aggregatorHandle = acquireHandle(labels);
    try {
      aggregatorHandle.recordDouble(value);
    } finally {
      aggregatorHandle.release();
    }
  }

  @Override
  public void record(double value) {
    record(value, Labels.empty());
  }

  @Override
  public BoundDoubleValueRecorder bind(Labels labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements BoundDoubleValueRecorder {
    private final AggregatorHandle<?> aggregatorHandle;

    BoundInstrument(AggregatorHandle<?> aggregatorHandle) {
      this.aggregatorHandle = aggregatorHandle;
    }

    @Override
    public void record(double value) {
      aggregatorHandle.recordDouble(value);
    }

    @Override
    public void unbind() {
      aggregatorHandle.release();
    }
  }

  static final class Builder
      extends AbstractSynchronousInstrumentBuilder<DoubleValueRecorderSdk.Builder>
      implements DoubleValueRecorderBuilder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(
          name,
          InstrumentType.VALUE_RECORDER,
          InstrumentValueType.DOUBLE,
          meterProviderSharedState,
          meterSharedState);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public DoubleValueRecorderSdk build() {
      return buildInstrument(DoubleValueRecorderSdk::new);
    }
  }
}
