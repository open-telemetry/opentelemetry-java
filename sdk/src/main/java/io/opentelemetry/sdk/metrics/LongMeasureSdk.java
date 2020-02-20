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

import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.LongMeasure;
import io.opentelemetry.sdk.metrics.LongMeasureSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongMeasureSdk extends AbstractMeasure<BoundInstrument> implements LongMeasure {

  private LongMeasureSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      boolean absolute) {
    super(
        descriptor, InstrumentValueType.LONG, meterProviderSharedState, meterSharedState, absolute);
  }

  @Override
  public void record(long value, LabelSet labelSet) {
    BoundInstrument boundInstrument = bind(labelSet);
    boundInstrument.record(value);
    boundInstrument.unbind();
  }

  @Override
  public BoundInstrument bind(LabelSet labelSet) {

    return bindInternal(labelSet);
  }

  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(isAbsolute(), batcher);
  }

  static final class BoundInstrument extends AbstractBoundInstrument implements BoundLongMeasure {

    private final boolean absolute;

    BoundInstrument(boolean absolute, Batcher batcher) {
      super(batcher.getAggregator());
      this.absolute = absolute;
    }

    @Override
    public void record(long value) {
      if (this.absolute && value < 0) {
        throw new IllegalArgumentException("absolute measure can only record positive values");
      }
      recordLong(value);
    }
  }

  static LongMeasure.Builder builder(
      String name,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState) {
    return new Builder(name, meterProviderSharedState, meterSharedState);
  }

  private static final class Builder
      extends AbstractMeasure.Builder<LongMeasure.Builder, LongMeasure>
      implements LongMeasure.Builder {

    private Builder(
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
    public LongMeasure build() {
      return new LongMeasureSdk(
          getInstrumentDescriptor(),
          getMeterProviderSharedState(),
          getMeterSharedState(),
          isAbsolute());
    }
  }
}
