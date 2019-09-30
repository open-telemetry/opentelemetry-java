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

import io.opentelemetry.metrics.GaugeDouble.Handle;
import java.util.List;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Gauge metric, to report instantaneous measurement of a double value. Gauges can go both up and
 * down. The gauges values can be negative.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = OpenTelemetry.getMeter();
 *   private static final GaugeDouble gauge =
 *       meter
 *           .gaugeDoubleBuilder("processed_jobs")
 *           .setDescription("Processed jobs")
 *           .setUnit("1")
 *           .setLabelKeys(Collections.singletonList("Key"))
 *           .build();
 *   // It is recommended to keep a reference of a Handle.
 *   private static final GaugeDouble.Handle someWorkHandle =
 *       gauge.getHandle(Collections.singletonList("SomeWork"));
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      someWorkHandle.set(15);
 *   }
 *
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface GaugeDouble extends Gauge<Handle> {

  @Override
  Handle getHandle(List<String> labelValues);

  @Override
  Handle getDefaultHandle();

  /**
   * A {@code Handle} for a {@code GaugeDouble}.
   *
   * @since 0.1.0
   */
  interface Handle {

    /**
     * Sets the given value.
     *
     * @param val the new value.
     * @since 0.1.0
     */
    void set(double val);
  }

  /** Builder class for {@link GaugeLong}. */
  interface Builder extends Gauge.Builder<Builder, GaugeDouble> {}
}
