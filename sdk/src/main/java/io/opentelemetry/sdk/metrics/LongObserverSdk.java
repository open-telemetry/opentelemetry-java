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
import java.util.List;
import java.util.Map;

final class LongObserverSdk extends AbstractInstrument implements LongObserver {
  private final boolean monotonic;

  LongObserverSdk(
      String name,
      String description,
      String unit,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      MeterSharedState sharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      boolean monotonic) {
    super(
        name,
        description,
        unit,
        constantLabels,
        labelKeys,
        getObserverInstrumentType(monotonic),
        InstrumentValueType.LONG,
        sharedState,
        instrumentationLibraryInfo);
    this.monotonic = monotonic;
  }

  @Override
  public void setCallback(Callback<LongObserver.ResultLongObserver> metricUpdater) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof LongObserverSdk)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    LongObserverSdk that = (LongObserverSdk) o;

    return monotonic == that.monotonic;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (monotonic ? 1 : 0);
    return result;
  }

  static LongObserver.Builder builder(
      String name,
      MeterSharedState sharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return new Builder(name, sharedState, instrumentationLibraryInfo);
  }

  private static final class Builder
      extends AbstractObserverBuilder<LongObserver.Builder, LongObserver>
      implements LongObserver.Builder {

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
    public LongObserver build() {
      return new LongObserverSdk(
          getName(),
          getDescription(),
          getUnit(),
          getConstantLabels(),
          getLabelKeys(),
          getMeterSharedState(),
          getInstrumentationLibraryInfo(),
          isMonotonic());
    }
  }
}
