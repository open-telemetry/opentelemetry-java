/*
 * Copyright 2019, OpenTelemetry Authors
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
import io.opentelemetry.metrics.DoubleUpDownCounter;
import io.opentelemetry.sdk.metrics.DoubleUpDownCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.Aggregations;

final class DoubleUpDownCounterSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements DoubleUpDownCounter {

  private DoubleUpDownCounterSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    super(
        descriptor,
        meterProviderSharedState,
        meterSharedState,
        new ActiveBatcher(
            Batchers.getCumulativeAllLabels(
                descriptor, meterProviderSharedState, meterSharedState, Aggregations.sum())));
  }

  @Override
  public void add(double increment, String... labelKeyValuePairs) {
    add(increment, Labels.of(labelKeyValuePairs));
  }

  public void add(double increment, Labels labelSetSdk) {
    BoundInstrument boundInstrument = bind(labelSetSdk);
    boundInstrument.add(increment);
    boundInstrument.unbind();
  }

  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(batcher);
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements BoundDoubleUpDownCounter {

    BoundInstrument(Batcher batcher) {
      super(batcher.getAggregator());
    }

    @Override
    public void add(double increment) {
      recordDouble(increment);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<DoubleUpDownCounterSdk.Builder>
      implements DoubleUpDownCounter.Builder {

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
    public DoubleUpDownCounterSdk build() {
      return register(
          new DoubleUpDownCounterSdk(
              getInstrumentDescriptor(InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.DOUBLE),
              getMeterProviderSharedState(),
              getMeterSharedState()));
    }
  }
}
