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

import io.opentelemetry.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** {@link MeterSdk} is SDK implementation of {@link Meter}. */
final class MeterSdk implements Meter {
  private final MeterProviderSharedState meterProviderSharedState;
  private final MeterSharedState meterSharedState;

  MeterSdk(
      MeterProviderSharedState meterProviderSharedState,
      InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.meterProviderSharedState = meterProviderSharedState;
    this.meterSharedState = MeterSharedState.create(instrumentationLibraryInfo);
  }

  InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return meterSharedState.getInstrumentationLibraryInfo();
  }

  @Override
  public DoubleCounterSdk.Builder doubleCounterBuilder(String name) {
    return new DoubleCounterSdk.Builder(name, meterProviderSharedState, meterSharedState);
  }

  @Override
  public LongCounterSdk.Builder longCounterBuilder(String name) {
    return new LongCounterSdk.Builder(name, meterProviderSharedState, meterSharedState);
  }

  @Override
  public DoubleMeasureSdk.Builder doubleMeasureBuilder(String name) {
    return new DoubleMeasureSdk.Builder(name, meterProviderSharedState, meterSharedState);
  }

  @Override
  public LongMeasureSdk.Builder longMeasureBuilder(String name) {
    return new LongMeasureSdk.Builder(name, meterProviderSharedState, meterSharedState);
  }

  @Override
  public DoubleObserverSdk.Builder doubleObserverBuilder(String name) {
    return new DoubleObserverSdk.Builder(name, meterProviderSharedState, meterSharedState);
  }

  @Override
  public LongObserverSdk.Builder longObserverBuilder(String name) {
    return new LongObserverSdk.Builder(name, meterProviderSharedState, meterSharedState);
  }

  @Override
  public BatchRecorderSdk newBatchRecorder(String... keyValuePairs) {
    return new BatchRecorderSdk(keyValuePairs);
  }

  Collection<MetricData> collectAll() {
    InstrumentRegistry instrumentRegistry = meterSharedState.getInstrumentRegistry();
    Collection<AbstractInstrument> instruments = instrumentRegistry.getInstruments();
    List<MetricData> result = new ArrayList<>(instruments.size());
    for (AbstractInstrument instrument : instruments) {
      result.addAll(instrument.collectAll());
    }
    return result;
  }
}
