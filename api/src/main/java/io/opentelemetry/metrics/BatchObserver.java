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

package io.opentelemetry.metrics;

import io.opentelemetry.common.Labels;
import io.opentelemetry.metrics.AsynchronousInstrument.Observation;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Util class that can be use to atomically record measurements associated with a set of Metrics.
 *
 * <p>This class is equivalent with individually calling record on every Measure, but has the
 * advantage that all these operations are recorded atomically and it is more efficient.
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
