/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongValueRecorderSdk extends AbstractSynchronousInstrument
    implements LongValueRecorder {

  private LongValueRecorderSdk(
      InstrumentDescriptor descriptor, SynchronousInstrumentAccumulator accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void record(long value, Labels labels) {
    Aggregator<?> aggregator = acquireHandle(labels);
    try {
      aggregator.recordLong(value);
    } finally {
      aggregator.release();
    }
  }

  @Override
  public void record(long value) {
    record(value, Labels.empty());
  }

  @Override
  public BoundLongValueRecorder bind(Labels labels) {
    return new BoundInstrument(acquireHandle(labels));
  }

  static final class BoundInstrument implements BoundLongValueRecorder {
    private final Aggregator<?> aggregator;

    BoundInstrument(Aggregator<?> aggregator) {
      this.aggregator = aggregator;
    }

    @Override
    public void record(long value) {
      aggregator.recordLong(value);
    }

    @Override
    public void unbind() {
      aggregator.release();
    }
  }

  static final class Builder
      extends AbstractSynchronousInstrumentBuilder<LongValueRecorderSdk.Builder>
      implements LongValueRecorder.Builder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(
          name,
          InstrumentType.VALUE_RECORDER,
          InstrumentValueType.LONG,
          meterProviderSharedState,
          meterSharedState);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public LongValueRecorderSdk build() {
      return buildInstrument(LongValueRecorderSdk::new);
    }
  }
}
