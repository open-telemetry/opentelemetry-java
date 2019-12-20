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

import io.opentelemetry.metrics.LongGauge.BoundLongGauge;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Gauge metric, to report instantaneous measurement of an long value. Gauges can go both up and
 * down. The gauges values can be negative.
 *
 * <p>Example:
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = OpenTelemetry.getMeterFactory().get("my_library_name");
 *   private static final LongGauge gauge =
 *       meter
 *           .gaugeLongBuilder("processed_jobs")
 *           .setDescription("Processed jobs")
 *           .setUnit("1")
 *           .setLabelKeys(Collections.singletonList("Key"))
 *           .build();
 *   // It is recommended to keep a reference to a Bound Metric.
 *   private static final BoundLongGauge someWorkBound =
 *       gauge.getBound(Collections.singletonList("SomeWork"));
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      someWorkBound.set(15);
 *   }
 *
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface LongGauge extends Gauge<BoundLongGauge> {

  /**
   * Sets the given value for the gauge.
   *
   * <p>The value added is associated with the current {@code Context} and provided LabelSet.
   *
   * @param val the new value.
   * @param labelSet the labels to be associated to this value
   * @since 0.1.0
   */
  void set(long val, LabelSet labelSet);

  @Override
  BoundLongGauge bind(LabelSet labelSet);

  @Override
  void unbind(BoundLongGauge bound);

  /**
   * A {@code Bound} for a {@code LongGauge}.
   *
   * @since 0.1.0
   */
  interface BoundLongGauge {

    /**
     * Sets the given value for the gauge.
     *
     * <p>The value added is associated with the current {@code Context}.
     *
     * @param val the new value.
     * @since 0.1.0
     */
    void set(long val);
  }

  /** Builder class for {@link LongGauge}. */
  interface Builder extends Gauge.Builder<Builder, LongGauge> {}
}
