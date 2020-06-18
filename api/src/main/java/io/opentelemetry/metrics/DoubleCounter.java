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
import io.opentelemetry.metrics.DoubleCounter.BoundDoubleCounter;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Counter is the most common synchronous instrument. This instrument supports an {@link
 * #add(double, String...)}` function for reporting an increment, and is restricted to non-negative
 * increments. The default aggregation is `Sum`.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *   private static final Meter meter = OpenTelemetry.getMeterRegistry().get("my_library_name");
 *   private static final DoubleCounter counter =
 *       meter.
 *           .doubleCounterBuilder("allocated_resources")
 *           .setDescription("Total allocated resources")
 *           .setUnit("1")
 *           .build();
 *
 *   // It is recommended that the API user keep references to a Bound Counters.
 *   private static final BoundDoubleCounter someWorkBound =
 *       counter.bind("work_name", "some_work");
 *
 *   void doSomeWork() {
 *      someWorkBound.add(10.2);  // Resources needed for this task.
 *      // Your code here.
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface DoubleCounter extends SynchronousInstrument<BoundDoubleCounter> {

  /**
   * Adds the given {@code increment} to the current value. The values cannot be negative.
   *
   * <p>The value added is associated with the current {@code Context} and provided set of labels.
   *
   * @param increment the value to add.
   * @param labelKeyValuePairs the labels to be associated to this recording.
   * @since 0.1.0
   */
  void add(double increment, String... labelKeyValuePairs);

  @Override
  BoundDoubleCounter bind(Labels labels);

  /**
   * A {@code Bound Instrument} for a {@link DoubleCounter}.
   *
   * @since 0.1.0
   */
  @ThreadSafe
  interface BoundDoubleCounter extends SynchronousInstrument.BoundInstrument {
    /**
     * Adds the given {@code increment} to the current value. The values cannot be negative.
     *
     * <p>The value added is associated with the current {@code Context}.
     *
     * @param increment the value to add.
     * @since 0.1.0
     */
    void add(double increment);

    @Override
    void unbind();
  }

  /** Builder class for {@link DoubleCounter}. */
  interface Builder extends SynchronousInstrument.Builder {
    @Override
    Builder setDescription(String description);

    @Override
    Builder setUnit(String unit);

    @Override
    Builder setConstantLabels(Labels constantLabels);

    @Override
    DoubleCounter build();
  }
}
