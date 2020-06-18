/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics;

import io.opentelemetry.common.Labels;
import io.opentelemetry.metrics.DoubleValueRecorder;
import io.opentelemetry.sdk.metrics.DoubleValueRecorderSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.Aggregations;

final class DoubleValueRecorderSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements DoubleValueRecorder {

  private DoubleValueRecorderSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    super(
        descriptor,
        meterProviderSharedState,
        meterSharedState,
        new ActiveBatcher(
            Batchers.getCumulativeAllLabels(
                descriptor,
                meterProviderSharedState,
                meterSharedState,
                Aggregations.minMaxSumCount())));
  }

  @Override
  public void record(double delta, String... labelKeyValuePairs) {
    record(delta, Labels.of(labelKeyValuePairs));
  }

  void record(double value, Labels labelSet) {
    BoundInstrument boundInstrument = bind(labelSet);
    boundInstrument.record(value);
    boundInstrument.unbind();
  }

  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(batcher);
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements BoundDoubleValueRecorder {

    BoundInstrument(Batcher batcher) {
      super(batcher.getAggregator());
    }

    @Override
    public void record(double value) {
      recordDouble(value);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<DoubleValueRecorderSdk.Builder>
      implements DoubleValueRecorder.Builder {

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(name, meterProviderSharedState, meterSharedState);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public DoubleValueRecorderSdk build() {
      return register(
          new DoubleValueRecorderSdk(
              getInstrumentDescriptor(InstrumentType.VALUE_RECORDER, InstrumentValueType.DOUBLE),
              getMeterProviderSharedState(),
              getMeterSharedState()));
    }
  }
}
