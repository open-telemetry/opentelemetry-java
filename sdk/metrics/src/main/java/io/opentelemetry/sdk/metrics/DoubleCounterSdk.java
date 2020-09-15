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
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.sdk.metrics.DoubleCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

/** @since 0.3.0 */
final class DoubleCounterSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements DoubleCounter {

  private DoubleCounterSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Batcher batcher) {
    super(descriptor, meterProviderSharedState, meterSharedState, new ActiveBatcher(batcher));
  }

  /** @since 0.6.0 */
  @Override
  public void add(double increment, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    try {
      boundInstrument.add(increment);
    } finally {
      boundInstrument.unbind();
    }
  }

  /** @since 0.8.0 */
  @Override
  public void add(double increment) {
    add(increment, Labels.empty());
  }

  /** @since 0.3.0 */
  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(batcher);
  }

  /** @since 0.3.0 */
  static final class BoundInstrument extends AbstractBoundInstrument
      implements DoubleCounter.BoundDoubleCounter {

    BoundInstrument(Batcher batcher) {
      super(batcher.getAggregator());
    }

    /** @since 0.3.0 */
    @Override
    public void add(double increment) {
      if (increment < 0) {
        throw new IllegalArgumentException("Counters can only increase");
      }
      recordDouble(increment);
    }
  }

  static final class Builder extends AbstractInstrument.Builder<DoubleCounterSdk.Builder>
      implements DoubleCounter.Builder {

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
    public DoubleCounterSdk build() {
      InstrumentDescriptor instrumentDescriptor =
          getInstrumentDescriptor(InstrumentType.COUNTER, InstrumentValueType.DOUBLE);
      return register(
          new DoubleCounterSdk(
              instrumentDescriptor,
              getMeterProviderSharedState(),
              getMeterSharedState(),
              getBatcher(instrumentDescriptor)));
    }
  }
}
