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
 *       Instrument. This should be used in libraries like gRPC to record measurements like
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
 *   private static final DoubleMeasure cacheHit = meter.measureDoubleBuilder("cache_hit").build();
 *
 *   Response serverBoundr(Request request) {
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
 *                   .getBound(Collections.singletonList(gcName))
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
 *   private static final DoubleGauge gauge =
 *       meter
 *           .longGaugeBuilder("queue_size")
 *           .setDescription("Pending jobs")
 *           .setUnit("1")
 *           .setLabelKeys(labelKeys)
 *           .build();
 *
 *   // It is recommended that API users keep a reference to a Bound Instrument.
 *   DoubleGauge.BoundDoubleGauge inboundBound = gauge.bind(labelValues);
 *
 *   void doAddElement() {
 *      // Your code here.
 *      inboundBound.add(1);
 *   }
 *
 *   void doRemoveElement() {
 *      inboundBound.add(-1);
 *      // Your code here.
 *   }
 *
 * }
 * }</pre>
 */
@ThreadSafe
public interface Meter {

  /**
   * Returns a builder for a {@link LongGauge}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code LongGauge.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongGauge.Builder longGaugeBuilder(String name);

  /**
   * Returns a builder for a {@link DoubleGauge}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code DoubleGauge.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleGauge.Builder doubleGaugeBuilder(String name);

  /**
   * Returns a builder for a {@link DoubleCounter}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code DoubleCounter.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleCounter.Builder doubleCounterBuilder(String name);

  /**
   * Returns a builder for a {@link LongCounter}.
   *
   * @param name the name of the metric. Should be a ASCII string with a length no greater than 255
   *     characters.
   * @return a {@code LongCounter.Builder}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongCounter.Builder longCounterBuilder(String name);

  /**
   * Returns a new builder for a {@link DoubleMeasure}.
   *
   * @param name Name of measure, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code DoubleMeasure}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleMeasure.Builder doubleMeasureBuilder(String name);

  /**
   * Returns a new builder for a {@link LongMeasure}.
   *
   * @param name Name of measure, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code LongMeasure}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongMeasure.Builder longMeasureBuilder(String name);

  /**
   * Returns a new builder for a {@link DoubleObserver}.
   *
   * @param name Name of observer, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code DoubleObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  DoubleObserver.Builder doubleObserverBuilder(String name);

  /**
   * Returns a new builder for a {@link LongObserver}.
   *
   * @param name Name of observer, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code LongObserver}.
   * @throws NullPointerException if {@code name} is null.
   * @throws IllegalArgumentException if different metric with the same name already registered.
   * @throws IllegalArgumentException if the {@code name} does not match the requirements.
   * @since 0.1.0
   */
  LongObserver.Builder longObserverBuilder(String name);

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

  /**
   * Returns an empty {@link LabelSet}. The implementation is permitted to have this be a singleton
   * instance.
   *
   * @return an empty {@link LabelSet}
   */
  LabelSet emptyLabelSet();
}
