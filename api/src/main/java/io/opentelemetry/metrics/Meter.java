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

import javax.annotation.concurrent.ThreadSafe;

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
 *   private static final Meter meter = Metrics.getMeterFactory().get("my_library_name");
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
 *   private static final Meter meter = Metrics.getMeterFactory().get("my_library_name");
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
 *          {@literal @}Override
 *           public void run() {
 *             for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
 *               LabelValue gcName = LabelValue.create(gc.getName());
 *               collectionMetric
 *                   .getHandle(Collections.singletonList(gcName))
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
 *   private static final Meter meter = Metrics.getMeterFactory().get("my_library_name");
 *   private static final List<String> keys = Collections.singletonList("Name");
 *   private static final List<String> values = Collections.singletonList("Inbound");
 *   private static final GaugeDouble gauge =
 *       meter
 *           .gaugeLongBuilder("queue_size")
 *           .setDescription("Pending jobs")
 *           .setUnit("1")
 *           .setLabelKeys(labelKeys)
 *           .build();
 *
 *   // It is recommended to keep a reference of a Handle.
 *   GaugeDouble.Handle inboundHandle = gauge.getHandle(labelValues);
 *
 *   void doAddElement() {
 *      // Your code here.
 *      inboundHandle.add(1);
 *   }
 *
 *   void doRemoveElement() {
 *      inboundHandle.add(-1);
 *      // Your code here.
 *   }
 *
 * }
 * }</pre>
 */
@ThreadSafe
public interface Meter {

  /**
   * Returns a builder for a {@link GaugeLong}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code GaugeLong.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  GaugeLong.Builder gaugeLongBuilder(String name);

  /**
   * Returns a builder for a {@link GaugeDouble}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code GaugeDouble.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  GaugeDouble.Builder gaugeDoubleBuilder(String name);

  /**
   * Returns a builder for a {@link CounterDouble}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code CounterDouble.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  CounterDouble.Builder counterDoubleBuilder(String name);

  /**
   * Returns a builder for a {@link CounterLong}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code CounterLong.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  CounterLong.Builder counterLongBuilder(String name);

  /**
   * Returns a new builder for a {@link MeasureDouble}.
   *
   * @param name Name of measure, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code MeasureDouble}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  MeasureDouble.Builder measureDoubleBuilder(String name);

  /**
   * Returns a new builder for a {@link MeasureLong}.
   *
   * @param name Name of measure, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code MeasureLong}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  MeasureLong.Builder measureLongBuilder(String name);

  /**
   * Returns a new builder for a {@link ObserverDouble}.
   *
   * @param name Name of observer, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code ObserverDouble}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  ObserverDouble.Builder observerDoubleBuilder(String name);

  /**
   * Returns a new builder for a {@link ObserverLong}.
   *
   * @param name Name of observer, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code ObserverLong}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  ObserverLong.Builder observerLongBuilder(String name);

  /**
   * Utility method that allows users to atomically record measurements to a set of Measures.
   *
   * @return a {@code MeasureBatchRecorder} that can be use to atomically record a set of
   *     measurements associated with different Measures.
   * @since 0.1.0
   */
  BatchRecorder newMeasureBatchRecorder();

  /**
   * Returns a new {@link LabelSet} with the given label.
   *
   * @param k1 first key.
   * @param v1 first value.
   * @return a new {@link LabelSet} with the given label.
   * @throws NullPointerException if any provided value is null.
   */
  LabelSet createLabelSet(String k1, String v1);

  /**
   * Returns a new {@link LabelSet} with the given labels.
   *
   * @param k1 first key.
   * @param v1 first value.
   * @param k2 second key.
   * @param v2 second value.
   * @return a new {@link LabelSet} with the given labels.
   * @throws NullPointerException if any provided value is null.
   */
  LabelSet createLabelSet(String k1, String v1, String k2, String v2);

  /**
   * Returns a new {@link LabelSet} with the given labels.
   *
   * @param k1 first key.
   * @param v1 first value.
   * @param k2 second key.
   * @param v2 second value.
   * @param k3 third key.
   * @param v3 third value.
   * @return a new {@link LabelSet} with the given labels.
   * @throws NullPointerException if any provided value is null.
   */
  LabelSet createLabelSet(String k1, String v1, String k2, String v2, String k3, String v3);

  /**
   * Returns a new {@link LabelSet} with the given labels.
   *
   * @param k1 first key.
   * @param v1 first value.
   * @param k2 second key.
   * @param v2 second value.
   * @param k3 third key.
   * @param v3 third value.
   * @param k4 fourth key.
   * @param v4 fourth value.
   * @return a new {@link LabelSet} with the given labels.
   * @throws NullPointerException if any provided value is null.
   */
  LabelSet createLabelSet(
      String k1, String v1, String k2, String v2, String k3, String v3, String k4, String v4);
}
