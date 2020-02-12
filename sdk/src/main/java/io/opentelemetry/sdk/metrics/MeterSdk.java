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
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.DoubleObserver;
import io.opentelemetry.metrics.LabelSet;
import io.opentelemetry.metrics.LongCounter;
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

  @Override
  public DoubleCounter.Builder doubleCounterBuilder(String name) {
    return DoubleCounterSdk.builder(name, sharedState);
  }

  @Override
  public LongCounter.Builder longCounterBuilder(String name) {
    return LongCounterSdk.builder(name, sharedState);
  }

  @Override
  public DoubleMeasure.Builder doubleMeasureBuilder(String name) {
    return DoubleMeasureSdk.builder(name, sharedState);
  }

  @Override
  public LongMeasure.Builder longMeasureBuilder(String name) {
    return LongMeasureSdk.builder(name, sharedState);
  }

  @Override
  public DoubleObserver.Builder doubleObserverBuilder(String name) {
    return DoubleObserverSdk.builder(name, sharedState);
  }

  @Override
  public LongObserver.Builder longObserverBuilder(String name) {
    return LongObserverSdk.builder(name, sharedState);
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
