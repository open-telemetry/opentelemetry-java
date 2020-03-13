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

import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.sdk.metrics.DoubleCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class DoubleCounterSdk extends AbstractCounter<BoundInstrument> implements DoubleCounter {

  private DoubleCounterSdk(
      InstrumentDescriptor descriptor,
      boolean monotonic,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    super(
        descriptor,
        InstrumentValueType.DOUBLE,
        meterProviderSharedState,
        meterSharedState,
        monotonic);
  }

  @Override
  public void add(double delta, String... labelKeyValuePairs) {
    add(delta, LabelSetSdk.create(labelKeyValuePairs));
  }

  void add(double delta, LabelSetSdk labelSet) {
    BoundInstrument boundInstrument = bind(labelSet);
    boundInstrument.add(delta);
    boundInstrument.unbind();
  }

  @Override
  public BoundInstrument bind(String... labelKeyValuePairs) {
    return bind(LabelSetSdk.create(labelKeyValuePairs));
  }

  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(isMonotonic(), batcher);
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements DoubleCounter.BoundDoubleCounter {

    private final boolean monotonic;

    BoundInstrument(boolean monotonic, Batcher batcher) {
      super(batcher.getAggregator());
      this.monotonic = monotonic;
    }

    @Override
    public void add(double delta) {
      if (monotonic && delta < 0) {
        throw new IllegalArgumentException("monotonic counters can only increase");
      }
      recordDouble(delta);
    }
  }

  static final class Builder extends AbstractCounter.Builder<DoubleCounterSdk.Builder>
      implements DoubleCounter.Builder {

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
    public DoubleCounterSdk build() {
      return register(
          new DoubleCounterSdk(
              getInstrumentDescriptor(),
              isMonotonic(),
              getMeterProviderSharedState(),
              getMeterSharedState()));
    }
  }
}
