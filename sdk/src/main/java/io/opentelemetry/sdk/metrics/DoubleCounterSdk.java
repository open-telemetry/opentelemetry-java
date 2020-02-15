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
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.List;
import java.util.Map;

final class DoubleCounterSdk extends AbstractCounter implements DoubleCounter {

  private DoubleCounterSdk(
      String name,
      String description,
      String unit,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      boolean monotonic,
      MeterSharedState sharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    super(
        name,
        description,
        unit,
        constantLabels,
        labelKeys,
        InstrumentValueType.DOUBLE,
        sharedState,
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
    return new BoundInstrument(isMonotonic());
  }

  static final class BoundInstrument extends AbstractBoundInstrument implements BoundDoubleCounter {

    private final boolean monotonic;

    BoundInstrument(boolean monotonic) {
      super(null);
      this.monotonic = monotonic;
    }

    @Override
    public void add(double delta) {
      if (monotonic && delta < 0) {
        throw new IllegalArgumentException("monotonic counters can only increase");
      }
      // TODO: pass through to an aggregator/accumulator
    }
  }

  static DoubleCounter.Builder builder(
      String name,
      MeterSharedState sharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return new Builder(name, sharedState, instrumentationLibraryInfo);
  }

  private static final class Builder
      extends AbstractCounterBuilder<DoubleCounter.Builder, DoubleCounter>
      implements DoubleCounter.Builder {

    private Builder(
        String name,
        MeterSharedState sharedState,
        InstrumentationLibraryInfo instrumentationLibraryInfo) {
      super(name, sharedState, instrumentationLibraryInfo);
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
          getMeterSharedState(),
          getInstrumentationLibraryInfo());
    }
  }
}
