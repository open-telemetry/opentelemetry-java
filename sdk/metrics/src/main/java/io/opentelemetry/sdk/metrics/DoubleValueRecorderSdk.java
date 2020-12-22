/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleValueRecorderSdk extends AbstractSynchronousInstrument
    implements DoubleValueRecorder {

  private DoubleValueRecorderSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void record(double value, Labels labels) {
    Aggregator aggregator = acquireHandle(labels);
    try {
      aggregator.recordDouble(value);
    } finally {
      aggregator.release();
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
    private final Aggregator aggregator;

    BoundInstrument(Aggregator aggregator) {
      this.aggregator = aggregator;
    }

    @Override
    public void record(double value) {
      aggregator.recordDouble(value);
    }

    @Override
    public void unbind() {
      aggregator.release();
    }
  }

  static final class Builder
      extends AbstractSynchronousInstrumentBuilder<DoubleValueRecorderSdk.Builder>
      implements DoubleValueRecorder.Builder {

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
