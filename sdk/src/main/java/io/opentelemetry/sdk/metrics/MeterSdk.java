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

import static java.util.Collections.singletonMap;

import com.google.common.collect.ImmutableMap;
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
import java.util.Map;

/** {@link MeterSdk} is SDK implementation of {@link Meter}. */
public class MeterSdk implements Meter {

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
    return SdkDoubleCounter.Builder.builder(name);
  }

  @Override
  public LongCounter.Builder longCounterBuilder(String name) {
    return SdkLongCounter.Builder.builder(name);
  }

  @Override
  public DoubleMeasure.Builder doubleMeasureBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public LongMeasure.Builder longMeasureBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public DoubleObserver.Builder doubleObserverBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public LongObserver.Builder longObserverBuilder(String name) {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public BatchRecorder newMeasureBatchRecorder() {
    throw new UnsupportedOperationException("to be implemented");
  }

  @Override
  public LabelSet createLabelSet(String k1, String v1) {
    return SdkLabelSet.create(singletonMap(k1, v1));
  }

  @Override
  public LabelSet createLabelSet(String k1, String v1, String k2, String v2) {
    return SdkLabelSet.create(ImmutableMap.of(k1, v1, k2, v2));
  }

  @Override
  public LabelSet createLabelSet(String k1, String v1, String k2, String v2, String k3, String v3) {
    return SdkLabelSet.create(ImmutableMap.of(k1, v1, k2, v2, k3, v3));
  }

  @Override
  public LabelSet createLabelSet(
      String k1, String v1, String k2, String v2, String k3, String v3, String k4, String v4) {
    return SdkLabelSet.create(ImmutableMap.of(k1, v1, k2, v2, k3, v3, k4, v4));
  }

  @Override
  public LabelSet createLabelSet(Map<String, String> labels) {
    return SdkLabelSet.create(labels);
  }

  @Override
  public LabelSet emptyLabelSet() {
    return SdkLabelSet.empty();
  }
}
