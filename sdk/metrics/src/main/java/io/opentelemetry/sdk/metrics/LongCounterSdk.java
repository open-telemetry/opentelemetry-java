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
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.sdk.metrics.LongCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongCounterSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements LongCounter {

  private LongCounterSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Batcher batcher) {
    super(descriptor, meterProviderSharedState, meterSharedState, new ActiveBatcher(batcher));
  }

  @Override
  public void add(long increment, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    try {
      boundInstrument.add(increment);
    } finally {
      boundInstrument.unbind();
    }
  }

  @Override
  public void add(long increment) {
    add(increment, Labels.empty());
  }

  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(batcher);
  }

  static final class BoundInstrument extends AbstractBoundInstrument
      implements LongCounter.BoundLongCounter {

    BoundInstrument(Batcher batcher) {
      super(batcher.getAggregator());
    }

    @Override
    public void add(long increment) {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
      recordLong(increment);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<LongCounterSdk.Builder>
      implements LongCounter.Builder {

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
    public LongCounterSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.COUNTER, InstrumentValueType.LONG);
      return register(
          new LongCounterSdk(
              instrumentDescriptor,
              getMeterProviderSharedState(),
              getMeterSharedState(),
              getBatcher(instrumentDescriptor)));
    }
  }
}
