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

import javax.annotation.concurrent.ThreadSafe;

/**
 * Util class that can be use to atomically record measurements associated with a set of Measures.
 *
 * <p>This class is equivalent with individually calling record on every Measure, but has the
 * advantage that all these operations are recorded atomically and it is more efficient.
 */
@ThreadSafe
public interface MeasureBatchRecorder {
  /**
   * Associates the {@link MeasureLong} with the given value. Subsequent updates to the same {@link
   * MeasureLong} will overwrite the previous value.
   *
   * @param measure the {@link MeasureLong}.
   * @param value the value to be associated with {@code measure}.
   * @return this.
   * @throws IllegalArgumentException if value is negative.
   * @since 0.1.0
   */
  MeasureBatchRecorder put(MeasureLong measure, long value);

  /**
   * Associates the {@link MeasureDouble} with the given value. Subsequent updates to the same
   * {@link MeasureDouble} will overwrite the previous value.
   *
   * @param measure the {@link MeasureDouble}.
   * @param value the value to be associated with {@code measure}.
   * @return this.
   * @throws IllegalArgumentException if value is negative.
   * @since 0.1.0
   */
  MeasureBatchRecorder put(MeasureDouble measure, double value);

  /**
   * Records all of the measures at the same time.
   *
   * <p>This method records all of the measurements every time it is called, so make sure it is not
   * called twice if not needed.
   *
   * @since 0.1.0
   */
  void record();
}
