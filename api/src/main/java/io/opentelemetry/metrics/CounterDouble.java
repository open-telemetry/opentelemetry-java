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

import io.opentelemetry.metrics.CounterDouble.BoundDoubleCounter;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Counter metric, to report instantaneous measurement of a double value. Cumulative values can go
 * up or stay the same, but can never go down. Cumulative values cannot be negative.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = OpenTelemetry.getMeterFactory().get("my_library_name");
 *   private static final CounterDouble counter =
 *       meter.
 *           .counterDoubleBuilder("processed_jobs")
 *           .setDescription("Processed jobs")
 *           .setUnit("1")
 *           .setLabelKeys(Collections.singletonList("Key"))
 *           .build();
 *   // It is recommended to keep a reference of a Bound.
 *   private static final BoundDoubleCounter someWorkBound =
 *       counter.getBound(Collections.singletonList("SomeWork"));
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      someWorkBound.add(10.0);
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface CounterDouble extends Counter<BoundDoubleCounter> {

  @Override
  BoundDoubleCounter getBound(LabelSet labelSet);

  @Override
  BoundDoubleCounter getDefaultBound();

  @Override
  void removeBound(BoundDoubleCounter bound);

  /**
   * A {@code Bound} for a {@code CounterDouble}.
   *
   * @since 0.1.0
   */
  interface BoundDoubleCounter {
    /**
     * Adds the given {@code delta} to the current value. The values can be negative iff monotonic
     * was set to {@code false}.
     *
     * <p>The value added is associated with the current {@code Context}.
     *
     * @param delta the value to add.
     * @since 0.1.0
     */
    void add(double delta);
  }

  /** Builder class for {@link CounterDouble}. */
  interface Builder extends Counter.Builder<Builder, CounterDouble> {}
}
