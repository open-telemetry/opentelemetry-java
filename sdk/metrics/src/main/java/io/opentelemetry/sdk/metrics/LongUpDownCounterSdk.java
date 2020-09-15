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
import io.opentelemetry.metrics.LongUpDownCounter;
import io.opentelemetry.sdk.metrics.LongUpDownCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

/** @since 0.5.0 */
final class LongUpDownCounterSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements LongUpDownCounter {

  private LongUpDownCounterSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Batcher batcher) {
    super(descriptor, meterProviderSharedState, meterSharedState, new ActiveBatcher(batcher));
  }

  /** @since 0.6.0 */
  @Override
  public void add(long increment, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    boundInstrument.add(increment);
    boundInstrument.unbind();
  }

  /** @since 0.8.0 */
  @Override
  public void add(long increment) {
    add(increment, Labels.empty());
  }

  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(batcher);
  }

  /** @since 0.5.0 */
  static final class BoundInstrument extends AbstractBoundInstrument
      implements BoundLongUpDownCounter {

    BoundInstrument(Batcher batcher) {
      super(batcher.getAggregator());
    }

    /** @since 0.5.0 */
    @Override
    public void add(long increment) {
      recordLong(increment);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<LongUpDownCounterSdk.Builder>
      implements LongUpDownCounter.Builder {

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
    public LongUpDownCounterSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.UP_DOWN_COUNTER, InstrumentValueType.LONG);
      return register(
          new LongUpDownCounterSdk(
              instrumentDescriptor,
              getMeterProviderSharedState(),
              getMeterSharedState(),
              getBatcher(instrumentDescriptor)));
    }
  }
}
