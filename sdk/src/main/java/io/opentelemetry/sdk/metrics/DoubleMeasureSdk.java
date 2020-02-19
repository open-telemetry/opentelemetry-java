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

import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.DoubleMeasureSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import java.util.List;
import java.util.Map;

final class DoubleMeasureSdk extends AbstractMeasure<BoundInstrument> implements DoubleMeasure {

  private DoubleMeasureSdk(
      String name,
      String description,
      String unit,
      Map<String, String> constantLabels,
      List<String> labelKeys,
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      boolean absolute) {
    super(
        name,
        description,
        unit,
        constantLabels,
        labelKeys,
        InstrumentValueType.DOUBLE,
        meterProviderSharedState,
        instrumentationLibraryInfo,
        absolute);
  }

  @Override
  public void record(double value, LabelSet labelSet) {
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

  static final class BoundInstrument extends AbstractBoundInstrument implements BoundDoubleMeasure {

    private final boolean absolute;

    BoundInstrument(boolean absolute, Batcher batcher) {
      super(batcher.getAggregator());
      this.absolute = absolute;
    }

    @Override
    public void record(double value) {
      if (this.absolute && value < 0) {
        throw new IllegalArgumentException("absolute measure can only record positive values");
      }
      recordDouble(value);
    }
  }

  static DoubleMeasure.Builder builder(
      String name,
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    return new Builder(name, meterProviderSharedState, instrumentationLibraryInfo);
  }

  private static final class Builder
      extends AbstractMeasure.Builder<DoubleMeasure.Builder, DoubleMeasure>
      implements DoubleMeasure.Builder {

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
    public DoubleMeasure build() {
      return new DoubleMeasureSdk(
          getName(),
          getDescription(),
          getUnit(),
          getConstantLabels(),
          getLabelKeys(),
          getMeterProviderSharedState(),
          getInstrumentationLibraryInfo(),
          isAbsolute());
    }
  }
}
