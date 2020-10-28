/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.sdk.metrics.LongValueRecorderSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongValueRecorderSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements LongValueRecorder {

  private LongValueRecorderSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Batcher batcher) {
    super(descriptor, meterProviderSharedState, meterSharedState, new ActiveBatcher(batcher));
  }

  @Override
  public void record(long value, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    boundInstrument.record(value);
    boundInstrument.unbind();
  }

  @Override
  public void record(long value) {
    record(value, Labels.empty());
  }

  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(batcher);
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements BoundLongValueRecorder {

    BoundInstrument(Batcher batcher) {
      super(batcher.getAggregator());
    }

    @Override
    public void record(long value) {
      recordLong(value);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<LongValueRecorderSdk.Builder>
      implements LongValueRecorder.Builder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState,
        MeterSdk meterSdk) {
      super(name, meterProviderSharedState, meterSharedState, meterSdk);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public LongValueRecorderSdk build() {
      InstrumentDescriptor descriptor =
          getInstrumentDescriptor(InstrumentType.VALUE_RECORDER, InstrumentValueType.LONG);
      return register(
          new LongValueRecorderSdk(
              descriptor,
              getMeterProviderSharedState(),
              getMeterSharedState(),
              getBatcher(descriptor)));
    }
  }
}
