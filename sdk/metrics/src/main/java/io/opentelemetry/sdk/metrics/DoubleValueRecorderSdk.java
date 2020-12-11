/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.sdk.metrics.DoubleValueRecorderSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleValueRecorderSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements DoubleValueRecorder {

  private DoubleValueRecorderSdk(
      InstrumentDescriptor descriptor,
      SynchronousInstrumentAccumulator<BoundInstrument> accumulator) {
    super(descriptor, accumulator);
  }

  @Override
  public void record(double value, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    boundInstrument.record(value);
    boundInstrument.unbind();
  }

  @Override
  public void record(double value) {
    record(value, Labels.empty());
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements BoundDoubleValueRecorder {

    BoundInstrument(Aggregator aggregator) {
      super(aggregator);
    }

    @Override
    public void record(double value) {
      recordDouble(value);
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
      return buildInstrument(BoundInstrument::new, DoubleValueRecorderSdk::new);
    }
  }
}
