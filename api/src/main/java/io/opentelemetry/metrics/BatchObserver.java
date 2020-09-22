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

package io.opentelemetry.metrics;

import io.opentelemetry.common.Labels;
import io.opentelemetry.metrics.AsynchronousInstrument.Observation;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Util class that can be use to atomically observe multiple instruments in one callback.
 *
 * <p>This class is equivalent with individually calling observe on every Measure, but has the
 * advantage that all these operations are recorded atomically.
 *
 * <p>Example:
 *
 * <pre>{@code
 * private void test() {
 *     final LongSumObserver distanceObserver =
 *         this.meter.longSumObserverBuilder("distance").setUnit("m").build();
 *     final DoubleSumObserver timeObserver =
 *         this.meter.doubleSumObserverBuilder("time").setUnit("s").build();
 *     this.meter.newBatchObserver(
 *         "exampleBatchObserver",
 *         new BatchObserverFunction() {
 *           {@literal @}Override
 *           public void observe(BatchObserverResult result) {
 *             MyMeasure measure = MyMeasure.getdata();
 *             result.observe(
 *                 Labels.of("myKey", "myalue"),
 *                 distanceObserver.observation(measure.getDistance()),
 *                 timeObserver.observation(measure.getTime())
 *             );
 *           }
 *         });
 *   }
 * }</pre>
 */
@ThreadSafe
public interface BatchObserver extends ObserverInstruments {

  interface BatchObserverFunction {
    void observe(BatchObserverResult result);
  }

  interface BatchObserverResult {
    void observe(Labels labels, Observation... observations);
  }
}
