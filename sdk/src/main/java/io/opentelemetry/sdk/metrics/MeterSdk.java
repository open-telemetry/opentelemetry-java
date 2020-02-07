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

import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleGauge;
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.DoubleObserver;
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongGauge;
import io.opentelemetry.metrics.LongMeasure;
import io.opentelemetry.metrics.LongObserver;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.Map;

/** {@link MeterSdk} is SDK implementation of {@link Meter}. */
final class MeterSdk implements Meter {
  private final MeterSharedState sharedState;
  private final InstrumentationLibraryInfo instrumentationLibraryInfo;

  MeterSdk(MeterSharedState sharedState, InstrumentationLibraryInfo instrumentationLibraryInfo) {
    this.sharedState = sharedState;
    this.instrumentationLibraryInfo = instrumentationLibraryInfo;
  }

  InstrumentationLibraryInfo getInstrumentationLibraryInfo() {
    return instrumentationLibraryInfo;
  }

  MeterSharedState getSharedState() {
    return sharedState;
  }

  @Override
  public LongGauge.Builder longGaugeBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public DoubleGauge.Builder doubleGaugeBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public DoubleCounter.Builder doubleCounterBuilder(String name) {
    return DoubleCounterSdk.builder(name);
  }

  @Override
  public LongCounter.Builder longCounterBuilder(String name) {
    return LongCounterSdk.builder(name);
  }

  @Override
  public DoubleMeasure.Builder doubleMeasureBuilder(String name) {
    return DoubleMeasureSdk.builder(name);
  }

  @Override
  public LongMeasure.Builder longMeasureBuilder(String name) {
    return LongMeasureSdk.builder(name);
  }

  @Override
  public DoubleObserver.Builder doubleObserverBuilder(String name) {
    return DoubleObserverSdk.builder(name);
  }

  @Override
  public LongObserver.Builder longObserverBuilder(String name) {
    return LongObserverSdk.builder(name);
  }

  @Override
  public BatchRecorder newBatchRecorder() {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public LabelSet createLabelSet(String... keyValuePairs) {
    return LabelSetSdk.create(keyValuePairs);
  }

  @Override
  public LabelSet createLabelSet(Map<String, String> labels) {
    return LabelSetSdk.create(labels);
  }
}
