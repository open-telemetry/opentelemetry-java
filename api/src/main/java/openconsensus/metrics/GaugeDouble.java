/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.metrics;

import java.util.List;
import javax.annotation.concurrent.ThreadSafe;
import openconsensus.metrics.GaugeDouble.TimeSeries;

/**
 * Gauge metric, to report instantaneous measurement of a double value. Gauges can go both up and
 * down. The gauges values can be negative.
 *
 * <p>Example 1: Create a Gauge with default labels.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = Metrics.getMeter();
 *   private static final MetricRegistry metricRegistry = meter.metricRegistryBuilder().build();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *
 *   GaugeDouble gauge = metricRegistry.addDoubleGauge("queue_size",
 *                       "Pending jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a TimeSeries.
 *   GaugeDouble.TimeSeries defaultTimeSeries = gauge.getDefaultTimeSeries();
 *
 *   void doWork() {
 *      // Your code here.
 *      defaultPoint.add(10);
 *   }
 *
 * }
 * }</pre>
 *
 * <p>Example 2: You can also use labels(keys and values) to track different types of metric.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = Metrics.getMeter();
 *   private static final MetricRegistry metricRegistry = meter.metricRegistryBuilder().build();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *   List<LabelValue> labelValues = Arrays.asList(LabelValue.create("Inbound"));
 *
 *   GaugeDouble gauge = metricRegistry.addDoubleGauge("queue_size",
 *                       "Pending jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a TimeSeries.
 *   GaugeDouble.TimeSeries inboundTimeSeries = gauge.getOrCreateTimeSeries(labelValues);
 *
 *   void doSomeWork() {
 *      // Your code here.
 *      inboundPoint.set(15);
 *   }
 *
 * }
 * }</pre>
 *
 * @since 0.1.0
 */
@ThreadSafe
public interface GaugeDouble extends Metric<TimeSeries> {

  @Override
  TimeSeries getOrCreateTimeSeries(List<LabelValue> labelValues);

  @Override
  TimeSeries getDefaultTimeSeries();

  /**
   * A {@code TimeSeries} for a {@code GaugeDouble}.
   *
   * @since 0.1.0
   */
  interface TimeSeries {

    /**
     * Adds the given value to the current value. The values can be negative.
     *
     * @param amt the value to add.
     * @since 0.1.0
     */
    void add(double amt);

    /**
     * Sets the given value.
     *
     * @param val the new value.
     * @since 0.1.0
     */
    void set(double val);
  }

  /** Builder class for {@link GaugeLong}. */
  interface Builder extends Metric.Builder<Builder, GaugeDouble> {}
}
