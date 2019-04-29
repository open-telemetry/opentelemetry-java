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
import openconsensus.tags.TagMap;
import openconsensus.trace.SpanContext;

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
 *   private static final Measure cacheHit = meter.measureBuilder("cache_hit").build();
 *
 *   Response serverHandler(Request request) {
 *     if (inCache(request)) {
 *       meter.record(Collections.singletonList(cacheHit.createMeasurement(1)));
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
 * public final void exportGarbageCollectorMetrics {
 *   final CounterLong collectionMetric =
 *       meter
 *           .counterLongBuilder("collection")
 *           .setDescription("Time spent in a given JVM garbage collector in milliseconds.")
 *           .setUnit("ms")
 *           .setLabelKeys(Collections.singletonList(GC))
 *           .build();
 *   collectionMetric.setCallback(
 *       new Runnable() {
 *         @Override
 *         public void run() {
 *           for (GarbageCollectorMXBean gc : ManagementFactory.getGarbageCollectorMXBeans()) {
 *             LabelValue gcName = LabelValue.create(gc.getName());
 *             collectionMetric
 *                 .getOrCreateTimeSeries(Collections.singletonList(gcName))
 *                 .set(gc.getCollectionTime());
 *           }
 *         }
 *       });
 *   }
 * }</pre>
 *
 * <p>Example usage for simple pre-defined aggregation metrics:
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
 *      inboundTimeSeries.add(15);
 *   }
 *
 * }
 * }</pre>
 */
public interface Meter {

  /**
   * Returns a new builder for a {@code MetricRegistry}.
   *
   * @return a new builder for a {@code MetricRegistry}.
   */
  // TODO: Consider to remove metric registry and move all methods in this class, or rename to a
  //  different name (maybe MetricsCollection). Also if an extra class is kept consider to move raw
  //  measurement API in it's own class as well.
  MetricRegistry.Builder metricRegistryBuilder();

  /**
   * Returns a new builder for a {@code Measure}.
   *
   * @param name Name of measure, as a {@code String}. Should be a ASCII string with a length no
   *     greater than 255 characters.
   * @return a new builder for a {@code Measure}.
   * @since 0.1.0
   */
  Measure.Builder measureBuilder(String name);

  /**
   * Records all given measurements, with the current {@link
   * openconsensus.tags.Tagger#getCurrentTagMap}.
   *
   * @param measurements the list of {@code Measurement}s to record.
   * @since 0.1.0
   */
  void record(List<Measurement> measurements);

  /**
   * Records all given measurements, with an explicit {@link TagMap}.
   *
   * @param measurements the list of {@code Measurement}s to record.
   * @param tags the tags associated with the measurements.
   * @since 0.1.0
   */
  void record(List<Measurement> measurements, TagMap tags);

  /**
   * Records all given measurements, with an explicit {@link TagMap}. These measurements are
   * associated with the given {@code SpanContext}.
   *
   * @param measurements the list of {@code Measurement}s to record.
   * @param tags the tags associated with the measurements.
   * @param spanContext the {@code SpanContext} that identifies the {@code Span} for which the
   *     measurements are associated with.
   * @since 0.1.0
   */
  // TODO: Avoid tracing dependency and accept Attachments as in OpenCensus.
  void record(List<Measurement> measurements, TagMap tags, SpanContext spanContext);
}
