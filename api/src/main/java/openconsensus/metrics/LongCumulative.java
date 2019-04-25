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

/**
 * Long Cumulative metric, to report instantaneous measurement of a long value. Cumulative values
 * can go up or stay the same, but can never go down. Cumulative values cannot be negative.
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
 *   LongCumulative cumulative = metricRegistry.addLongCumulative("processed_jobs",
 *                       "Processed jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a point for manual operations.
 *   LongCumulative.TimeSeries defaultTimeSeries = cumulative.getDefaultTimeSeries();
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
 *   LongCumulative cumulative = metricRegistry.addLongCumulative("processed_jobs",
 *                       "Processed jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a point for manual operations.
 *   LongCumulative.TimeSeries inboundTimeSeries = cumulative.getOrCreateTimeSeries(labelValues);
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
public interface LongCumulative extends Metric {

  /**
   * Creates a {@code TimeSeries} and returns a {@code TimeSeries} if the specified {@code
   * labelValues} is not already associated with this cumulative, else returns an existing {@code
   * TimeSeries}.
   *
   * <p>It is recommended to keep a reference to the TimeSeries instead of always calling this
   * method for manual operations.
   *
   * @param labelValues the list of label values. The number of label values must be the same to
   *     that of the label keys passed to {@link Builder#setLabelKeys(List)}.
   * @return a {@code TimeSeries} the value of single cumulative.
   * @throws NullPointerException if {@code labelValues} is null OR any element of {@code
   *     labelValues} is null.
   * @throws IllegalArgumentException if number of {@code labelValues}s are not equal to the label
   *     keys.
   * @since 0.1.0
   */
  TimeSeries getOrCreateTimeSeries(List<LabelValue> labelValues);

  /**
   * Returns a {@code TimeSeries} for a cumulative with all labels not set, or default labels.
   *
   * @return a {@code TimeSeries} for a cumulative with all labels not set, or default labels.
   * @since 0.1.0
   */
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
  }

  /** Builder class for {@link LongCumulative}. */
  interface Builder extends Metric.Builder<Builder, LongCumulative> {}
}
