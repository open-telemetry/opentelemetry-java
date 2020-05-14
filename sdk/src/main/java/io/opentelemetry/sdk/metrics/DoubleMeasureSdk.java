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

import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.sdk.metrics.DoubleMeasureSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.Aggregations;

final class DoubleMeasureSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements DoubleMeasure {

  private DoubleMeasureSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    super(
        descriptor,
        meterProviderSharedState,
        meterSharedState,
        new ActiveBatcher(
            getDefaultBatcher(
                descriptor,
                meterProviderSharedState,
                meterSharedState,
                Aggregations.minMaxSumCount())));
  }

  @Override
  public void record(double delta, String... labelKeyValuePairs) {
    record(delta, LabelSetSdk.create(labelKeyValuePairs));
  }

  void record(double value, LabelSetSdk labelSet) {
    BoundInstrument boundInstrument = bind(labelSet);
    boundInstrument.record(value);
    boundInstrument.unbind();
  }

  @Override
  public BoundInstrument bind(String... labelKeyValuePairs) {
    return bind(LabelSetSdk.create(labelKeyValuePairs));
  }

  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(batcher);
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements DoubleMeasure.BoundDoubleMeasure {

    BoundInstrument(Batcher batcher) {
      super(batcher.getAggregator());
    }

    @Override
    public void record(double value) {
      recordDouble(value);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<DoubleMeasureSdk.Builder>
      implements DoubleMeasure.Builder {

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
    public DoubleMeasureSdk build() {
      return register(
          new DoubleMeasureSdk(
              getInstrumentDescriptor(
                  InstrumentType.MEASURE_NON_ABSOLUTE, InstrumentValueType.DOUBLE),
              getMeterProviderSharedState(),
              getMeterSharedState()));
    }
  }
}
