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

package io.opentelemetry.contrib.http.core;

import io.opentelemetry.metrics.BatchRecorder;
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.LongMeasure;

/** Used to make implementations work until SDK implementation is available. */
public class TemporaryMeasureBatchRecorder implements BatchRecorder {

  @Override
  public BatchRecorder put(LongMeasure measure, long value) {
    return this;
  }

  @Override
  public BatchRecorder put(DoubleMeasure measure, double value) {
    return this;
  }

  @Override
  public void record() {}
}
