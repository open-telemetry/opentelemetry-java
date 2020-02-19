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

import io.opentelemetry.metrics.LongObserver;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;

final class LongObserverSdk extends AbstractObserver implements LongObserver {
  LongObserverSdk(
      InstrumentDescriptor descriptor,
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      boolean monotonic) {
    super(
        descriptor,
        InstrumentValueType.LONG,
        meterProviderSharedState,
        instrumentationLibraryInfo,
        monotonic);
  }

  @Override
  public void setCallback(Callback<LongObserver.ResultLongObserver> metricUpdater) {
    throw new UnsupportedOperationException("to be implemented");
  }

  static LongObserver.Builder builder(
      String name,
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return new Builder(name, meterProviderSharedState, instrumentationLibraryInfo);
  }

  private static final class Builder
      extends AbstractObserver.Builder<LongObserver.Builder, LongObserver>
      implements LongObserver.Builder {

    private Builder(
        String name,
        MeterProviderSharedState meterProviderSharedState,
        InstrumentationLibraryInfo instrumentationLibraryInfo) {
      super(name, meterProviderSharedState, instrumentationLibraryInfo);
    }

    @Override
    Builder getThis() {
      return this;
    }

    @Override
    public LongObserver build() {
      return new LongObserverSdk(
          getInstrumentDescriptor(),
          getMeterProviderSharedState(),
          getInstrumentationLibraryInfo(),
          isMonotonic());
    }
  }
}
