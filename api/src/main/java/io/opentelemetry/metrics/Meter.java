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

/**
 * Meter is a simple, interface that allows users to record measurements (metrics).
 *
 * <p>There are two ways to record measurements:
 *
 * <ul>
 *   <li>Record raw measurements, and defer defining the aggregation and the labels for the exported
 *       Metric. This should be used in libraries like gRPC to record measurements like
 *       "server_latency" or "received_bytes".
 *   <li>Record pre-defined aggregation data (or already aggregated data). This should be used to
 *       report cpu/memory usage, or simple metrics like "queue_length".
 * </ul>
 *
 * <p>Example usage for raw measurement:
 *
 * <pre>{@code
 * class MyClass {
 *   private static final Meter meter = Metrics.getMeter();
 *   private static final MeasureDouble cacheHit = meter.measureDoubleBuilder("cache_hit").build();
 *
 *   Response serverHandler(Request request) {
 *     if (inCache(request)) {
 *       cacheHit.record(1);
 *       return fromCache(request);
 *     }
 *     ...  // do other work
 *   }
 *
 * }
 * }</pre>
 *
 * <p>Example usage for already aggregated metrics:
 *
 * <pre>{@code
 * class YourClass {
 *   private static final Meter meter = Metrics.getMeter();
 *   private static final CounterLong collectionMetric =
 *       meter
 *           .counterLongBuilder("collection")
 *           .setDescription("Time spent in a given JVM garbage collector in milliseconds.")
 *           .setUnit("ms")
 *           .setLabelKeys(Collections.singletonList(GC))
 *           .build();
 *
 *   public final void exportGarbageCollectorMetrics {
 *     collectionMetric.setCallback(
 *         new Runnable() {
 *           &commat;Override
 *           public void run() {
 *             for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
 *               LabelValue gcName = LabelValue.create(gc.getName());
 *               collectionMetric
 *                   .getOrCreateTimeSeries(Collections.singletonList(gcName))
 *                   .set(gc.getCollectionTime());
 *             }
 *           }
 *         });
 *   }
 * }
 * }</pre>
 *
 * <p>Example usage for simple pre-defined aggregation metrics:
 *
 * <pre>{@code
 * class YourClass {
 *   private static final Meter meter = Metrics.getMeter();
 *   private static final List<LabelKey> keys = Arrays.asList(LabelKey.create("Name", "desc"));
 *   private static final List<LabelValue> values = Arrays.asList(LabelValue.create("Inbound"));
 *   private static final GaugeDouble gauge = metricRegistry.gaugeLongBuilder(
 *       "queue_size", "Pending jobs", "1", labelKeys);
 *
 *   // It is recommended to keep a reference of a TimeSeries.
 *   GaugeDouble.TimeSeries inboundTimeSeries = gauge.getOrCreateTimeSeries(labelValues);
 *
 *   void doAddElement() {
 *      // Your code here.
 *      inboundTimeSeries.add(1);
 *   }
 *
 *   void doRemoveElement() {
 *      inboundTimeSeries.add(-1);
 *      // Your code here.
 *   }
 *
 * }
 * }</pre>
 */
public interface Meter {

  /**
   * Returns a builder for a {@link GaugeLong}.
   *
   * @param name the name of the metric.
   * @return a {@code GaugeLong.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  GaugeLong.Builder gaugeLongBuilder(String name);

  /**
   * Returns a builder for a {@link GaugeDouble}.
   *
   * @param name the name of the metric.
   * @return a {@code GaugeDouble.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  GaugeDouble.Builder gaugeDoubleBuilder(String name);

  /**
   * Returns a builder for a {@link CounterDouble}.
   *
   * @param name the name of the metric.
   * @return a {@code CounterDouble.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  CounterDouble.Builder counterDoubleBuilder(String name);

  /**
   * Returns a builder for a {@link CounterLong}.
   *
   * @param name the name of the metric.
   * @return a {@code CounterLong.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @since 0.1.0
   */
  CounterLong.Builder counterLongBuilder(String name);

  /**
   * Returns a new builder for a {@link MeasureDouble}.
   *
   * @param name Name of measure, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code Measure}.
   * @since 0.1.0
   */
  MeasureDouble.Builder measureDoubleBuilder(String name);

  /**
   * Returns a new builder for a {@link MeasureLong}.
   *
   * @param name Name of measure, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code Measure}.
   * @since 0.1.0
   */
  MeasureLong.Builder measureLongBuilder(String name);
}
