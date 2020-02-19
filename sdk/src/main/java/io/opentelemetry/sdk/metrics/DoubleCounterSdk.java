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
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.DoubleCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.List;
import java.util.Map;

final class DoubleCounterSdk extends AbstractCounter<BoundInstrument> implements DoubleCounter {

  private DoubleCounterSdk(
      String name,
      String description,
      String unit,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      boolean monotonic,
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    super(
        name,
        description,
        unit,
        constantLabels,
        labelKeys,
        InstrumentValueType.DOUBLE,
        meterProviderSharedState,
        instrumentationLibraryInfo,
        monotonic);
  }

  @Override
  public void add(double delta, LabelSet labelSet) {
    BoundInstrument boundInstrument = bind(labelSet);
    boundInstrument.add(delta);
    boundInstrument.unbind();
  }

  @Override
  public BoundInstrument bind(LabelSet labelSet) {
    return bindInternal(labelSet);
  }

  @Override
  BoundInstrument newBinding(Batcher batcher) {
    return new BoundInstrument(isMonotonic(), batcher);
  }

  static final class BoundInstrument extends AbstractBoundInstrument implements BoundDoubleCounter {

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

  static DoubleCounter.Builder builder(
      String name,
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return new Builder(name, meterProviderSharedState, instrumentationLibraryInfo);
  }

  private static final class Builder
      extends AbstractCounter.Builder<DoubleCounter.Builder, DoubleCounter>
      implements DoubleCounter.Builder {

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
    public DoubleCounter build() {
      return new DoubleCounterSdk(
          getName(),
          getDescription(),
          getUnit(),
          getConstantLabels(),
          getLabelKeys(),
          isMonotonic(),
          getMeterProviderSharedState(),
          getInstrumentationLibraryInfo());
    }
  }
}
