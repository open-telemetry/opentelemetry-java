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

import io.opentelemetry.metrics.CounterDouble.Handle;
import java.util.List;
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
 *   private static final Meter meter = OpenTelemetry.getMeter();
 *   private static final CounterDouble counter =
 *       meter.
 *           .counterDoubleBuilder("processed_jobs")
 *           .setDescription("Processed jobs")
 *           .setUnit("1")
 *           .setLabelKeys(Collections.singletonList("Key"))
 *           .build();
 *   // It is recommended to keep a reference of a Handle.
 *   private static final CounterDouble.Handle someWorkHandle =
 *       counter.getHandle(Collections.singletonList("SomeWork"));
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      someWorkHandle.add(10.0);
 *   }
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface CounterDouble extends Counter<Handle> {

  @Override
  Handle getHandle(List<String> labelValues);

  @Override
  Handle getDefaultHandle();

  /**
   * A {@code Handle} for a {@code CounterDouble}.
   *
   * @since 0.1.0
   */
  interface Handle {

    /**
     * Adds the given {@code delta} to the current value. The values can be negative iff monotonic
     * was set to {@code false}.
     *
     * @param delta the value to add
     * @since 0.1.0
     */
    void add(double delta);
  }

  /** Builder class for {@link CounterDouble}. */
  interface Builder extends Counter.Builder<Builder, CounterDouble> {}
}
