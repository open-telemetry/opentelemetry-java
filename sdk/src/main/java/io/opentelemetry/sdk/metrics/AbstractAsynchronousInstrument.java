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

import io.opentelemetry.metrics.AsynchronousInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.view.Aggregations;

abstract class AbstractAsynchronousInstrument extends AbstractInstrument {
  private final boolean monotonic;

  AbstractAsynchronousInstrument(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      MeterSharedState meterSharedState,
      boolean monotonic) {
    super(
        descriptor,
        meterProviderSharedState,
        meterSharedState,
        new ActiveBatcher(
            new ActiveBatcher(
                getDefaultBatcher(
                    descriptor,
                    meterProviderSharedState,
                    meterSharedState,
                    Aggregations.lastValue()))));
    this.monotonic = monotonic;
  }

  final boolean isMonotonic() {
    return monotonic;
  }

  abstract static class Builder<B extends AbstractAsynchronousInstrument.Builder<B>>
      extends AbstractInstrument.Builder<B> implements AsynchronousInstrument.Builder {
    private boolean monotonic = false;

    Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        MeterSharedState meterSharedState) {
      super(name, meterProviderSharedState, meterSharedState);
    }

    @Override
    public final B setMonotonic(boolean monotonic) {
      this.monotonic = monotonic;
      return getThis();
    }

    final boolean isMonotonic() {
      return this.monotonic;
    }
  }

  static InstrumentType getInstrumentType(boolean monotonic) {
    return monotonic ? InstrumentType.OBSERVER_MONOTONIC : InstrumentType.OBSERVER_NON_MONOTONIC;
  }
}
