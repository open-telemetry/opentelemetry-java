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
 * Measure to report instantaneous measurement of a double value.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = OpenTelemetry.getMeterFactory().get("my_library_name");
 *   private static final MeasureDouble measure =
 *       meter.
 *           .measureDoubleBuilder("doWork_latency")
 *           .setDescription("gRPC Latency")
 *           .setUnit("ms")
 *           .build();
 *   private static final MeasureDouble.Handle defaultHandle = measure.getDefaultHandle();
 *
 *   void doWork() {
 *      long startTime = System.nanoTime();
 *      // Your code here.
 *      defaultHandle.record((System.nanoTime() - startTime) / 1e6);
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface MeasureDouble extends Measure<MeasureDouble.Handle> {
  /**
   * A {@code Handle} for a {@code MeasureLong}.
   *
   * @since 0.1.0
   */
  @ThreadSafe
  interface Handle {
    /**
     * Records the given measurement, associated with the current {@code Context}.
     *
     * @param value the measurement to record.
     * @throws IllegalArgumentException if value is negative.
     * @since 0.1.0
     */
    void record(double value);
  }

  /** Builder class for {@link MeasureDouble}. */
  interface Builder extends Measure.Builder<Builder, MeasureDouble> {}
}
