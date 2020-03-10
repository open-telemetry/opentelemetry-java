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

import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.LongCounter;
import io.opentelemetry.metrics.LongMeasure;

/**
 * Minimal implementation of the {@link BatchRecorder} that simply redirects the calls to the
 * instruments.
 *
 * <p>TODO: Add an async queue processing to process batch records.
 */
final class BatchRecorderSdk implements BatchRecorder {
  private final LabelSetSdk labelSet;

  BatchRecorderSdk(String... keyValuePairs) {
    this.labelSet = LabelSetSdk.create(keyValuePairs);
  }

  @Override
  public BatchRecorder put(LongMeasure measure, long value) {
    ((LongMeasureSdk) measure).record(value, labelSet);
    return this;
  }

  @Override
  public BatchRecorder put(DoubleMeasure measure, double value) {
    ((DoubleMeasureSdk) measure).record(value, labelSet);
    return this;
  }

  @Override
  public BatchRecorder put(LongCounter counter, long value) {
    ((LongCounterSdk) counter).add(value, labelSet);
    return this;
  }

  @Override
  public BatchRecorder put(DoubleCounter counter, double value) {
    ((DoubleCounterSdk) counter).add(value, labelSet);
    return this;
  }

  @Override
  public void record() {
    // No-op in this minimal implementation.
  }
}
