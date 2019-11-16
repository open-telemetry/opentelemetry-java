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

import io.opentelemetry.metrics.CounterDouble;
import io.opentelemetry.metrics.CounterLong;
import io.opentelemetry.metrics.GaugeDouble;
import io.opentelemetry.metrics.GaugeLong;
import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.metrics.MeasureDouble;
import io.opentelemetry.metrics.MeasureLong;
import io.opentelemetry.metrics.Meter;
import io.opentelemetry.metrics.ObserverDouble;
import io.opentelemetry.metrics.ObserverLong;

/** {@link MeterSdk} is SDK implementation of {@link Meter}. */
public class MeterSdk implements Meter {

  @Override
  public GaugeLong.Builder gaugeLongBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public GaugeDouble.Builder gaugeDoubleBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public CounterDouble.Builder counterDoubleBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public CounterLong.Builder counterLongBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public MeasureDouble.Builder measureDoubleBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public MeasureLong.Builder measureLongBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public ObserverDouble.Builder observerDoubleBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public ObserverLong.Builder observerLongBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public BatchRecorder newMeasureBatchRecorder() {
    throw new UnsupportedOperationException("to be implemented");
  }
}
