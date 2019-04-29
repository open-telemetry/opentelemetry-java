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
import openconsensus.metrics.CounterLong.TimeSeries;

/**
 * Counter metric, to report instantaneous measurement of a long value. Cumulative values can go up
 * or stay the same, but can never go down. Cumulative values cannot be negative.
 *
 * <p>Example 1: Create a Cumulative with default labels.
 *
 * <pre>{@code
 * class YourClass {
 *
 *   private static final Meter meter = Metrics.getMeter();
 *   private static final MetricRegistry metricRegistry = meter.metricRegistryBuilder().build();
 *
 *   List<LabelKey> labelKeys = Arrays.asList(LabelKey.create("Name", "desc"));
 *
 *   CounterLong cumulative = metricRegistry.addLongCumulative("processed_jobs",
 *                       "Processed jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a TimeSeries.
 *   CounterLong.TimeSeries defaultTimeSeries = cumulative.getDefaultTimeSeries();
 *
 *   void doWork() {
 *      // Your code here.
 *      defaultPoint.add(10);
 *   }
 *
 * }
 * }</pre>
 *
 * <p>Example 2: You can also use labels (keys and values) to track different types of metric.
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
 *   CounterLong cumulative = metricRegistry.addLongCumulative("processed_jobs",
 *                       "Processed jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a TimeSeries.
 *   CounterLong.TimeSeries inboundTimeSeries = cumulative.getOrCreateTimeSeries(labelValues);
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
public interface CounterLong extends Metric<TimeSeries> {

  @Override
  TimeSeries getOrCreateTimeSeries(List<LabelValue> labelValues);

  @Override
  TimeSeries getDefaultTimeSeries();

  /**
   * The value of a single point in the Cumulative.TimeSeries.
   *
   * @since 0.1.0
   */
  interface TimeSeries {

    /**
     * Adds the given value to the current value. The values cannot be negative.
     *
     * @param delta the value to add
     * @since 0.1.0
     */
    void add(long delta);

    /**
     * Sets the given value. The value must be larger than the current recorded value.
     *
     * <p>In general should be used in combination with {@link #setCallback(Runnable)} where the
     * recorded value is guaranteed to be monotonically increasing.
     *
     * @param val the new value.
     * @since 0.1.0
     */
    void set(long val);
  }

  /** Builder class for {@link CounterLong}. */
  interface Builder extends Metric.Builder<Builder, CounterLong> {}
}
