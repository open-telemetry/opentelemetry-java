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

/** @since 0.1.0 */
final class LongValueRecorderSdk extends AbstractSynchronousInstrument<BoundInstrument>
    implements LongValueRecorder {

  private LongValueRecorderSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      Batcher batcher) {
    super(descriptor, meterProviderSharedState, meterSharedState, new ActiveBatcher(batcher));
  }

  /** @since 0.3.0 */
  @Override
  public void record(long value, Labels labels) {
    BoundInstrument boundInstrument = bind(labels);
    boundInstrument.record(value);
    boundInstrument.unbind();
  }

  /** @since 0.8.0 */
  @Override
  public void record(long value) {
    record(value, Labels.empty());
  }

  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(batcher);
  }

  /** @since 0.1.0 */
  static final class BoundInstrument extends AbstractBoundInstrument
      implements BoundLongValueRecorder {

    BoundInstrument(Batcher batcher) {
      super(batcher.getAggregator());
    }

    /** @since 0.1.0 */
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
