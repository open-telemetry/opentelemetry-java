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

import io.opentelemetry.metrics.LongCounter.BoundLongCounter;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Counter instrument, to report instantaneous measurements of long values. Cumulative values can go
 * up or stay the same, but can never go down. Cumulative values cannot be negative.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = OpenTelemetry.getMeterRegistry().get("my_library_name");
 *   private static final LongCounter counter =
 *       meter.
 *           .longCounterBuilder("processed_jobs")
 *           .setDescription("Processed jobs")
 *           .setUnit("1")
 *           .build();
 *   // It is recommended that the API user keep a reference to a Bound Counter.
 *   private static final BoundLongCounter someWorkBound =
 *       counter.getBound(Collections.singletonList("SomeWork"));
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      someWorkBound.add(10);
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface LongCounter extends Counter<BoundLongCounter> {

  /**
   * Adds the given {@code delta} to the current value. The values can be negative iff monotonic was
   * set to {@code false}.
   *
   * <p>The value added is associated with the current {@code Context} and provided set of labels.
   *
   * @param delta the value to add.
   * @param labelKeyValuePairs the set of labels to be associated to this recording.
   * @since 0.1.0
   */
  void add(long delta, String... labelKeyValuePairs);

  @Override
  BoundLongCounter bind(String... labelKeyValuePairs);

  /**
   * A {@code Bound Instrument} for a {@code LongCounter}.
   *
   * @since 0.1.0
   */
  @ThreadSafe
  interface BoundLongCounter extends InstrumentWithBinding.BoundInstrument {

    /**
     * Adds the given {@code delta} to the current value. The values can be negative iff monotonic
     * was set to {@code false}.
     *
     * <p>The value added is associated with the current {@code Context}.
     *
     * @param delta the value to add.
     * @since 0.1.0
     */
    void add(long delta);

    @Override
    void unbind();
  }

  /** Builder class for {@link LongCounter}. */
  interface Builder extends Counter.Builder {
    @Override
    Builder setDescription(String description);

    @Override
    Builder setUnit(String unit);

    @Override
    Builder setConstantLabels(Map<String, String> constantLabels);

    @Override
    Builder setMonotonic(boolean monotonic);

    @Override
    LongCounter build();
  }
}
