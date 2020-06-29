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
import io.opentelemetry.metrics.LongValueRecorder;
import io.opentelemetry.sdk.metrics.LongValueRecorderSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.view.Aggregations;

final class LongValueRecorderSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements LongValueRecorder {

  private LongValueRecorderSdk(
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
  public void record(long value, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    boundInstrument.record(value);
    boundInstrument.unbind();
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
        MeterSharedState meterSharedState) {
      super(name, meterProviderSharedState, meterSharedState);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public LongValueRecorderSdk build() {
      return register(
          new LongValueRecorderSdk(
              getInstrumentDescriptor(InstrumentType.VALUE_RECORDER, InstrumentValueType.LONG),
              getMeterProviderSharedState(),
              getMeterSharedState()));
    }
  }
}
