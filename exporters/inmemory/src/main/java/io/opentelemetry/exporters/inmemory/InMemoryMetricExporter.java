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

package io.opentelemetry.exporters.inmemory;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A {@link MetricExporter} implementation that can be used to test OpenTelemetry integration.
 *
 * <p>Can be created using {@code InMemoryMetricExporter.create()}
 *
 * <p>Example usage:
 *
 * <pre><code>
 * public class InMemoryMetricExporterExample {
 *
 *   // creating InMemoryMetricExporter
 *   private final InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
 *   private final MeterSdkProvider meterSdkProvider = OpenTelemetrySdk.getMeterProvider();
 *   private final Meter meter = meterSdkProvider.get("InMemoryMetricExporterExample");
 *   private IntervalMetricReader intervalMetricReader;
 *
 *   void setup() {
 *     intervalMetricReader =
 *         IntervalMetricReader.builder()
 *             .setMetricExporter(exporter)
 *             .setMetricProducers(Collections.singletonList(meterSdkProvider.getMetricProducer()))
 *             .setExportIntervalMillis(1000)
 *             .build();
 *   }
 *
 *   List&lt;MetricData&gt; readExportedMetrics() {
 *     return exporter.getFinishedMetricItems();
 *   }
 *
 *   void shutdown() {
 *     intervalMetricReader.shutdown();
 *   }
 *
 *   LongCounter generateLongMeter(String name) {
 *     return meter.longCounterBuilder(name).setDescription("Sample LongCounter").build();
 *   }
 *
 *   DoubleCounter generateDoubleMeter(String name) {
 *     return meter.doubleCounterBuilder(name).setDescription("Sample DoubleCounter").build();
 *   }
 *
 *
 *   public static void main(String[] args) throws InterruptedException {
 *     InMemoryMetricExporterExample example = new InMemoryMetricExporterExample();
 *
 *     example.setup();
 *     example.generateLongMeter("counter-1");
 *     example.generateDoubleMeter("counter-2");
 *     example.generateLongMeter("counter-3");
 *     // Delaying so that IntervalMetricReader could pull Metrics from MetricProducer and push them to
 *     // MetricExporter
 *     Thread.sleep(2000);
 *     List&lt;MetricData&gt; metricDataList = example.readExportedMetrics();
 *     System.out.println(metricDataList);
 *     example.shutdown();
 *     System.out.println("Bye");
 *   }
 * }
 * </code></pre>
 */
public class InMemoryMetricExporter implements MetricExporter {

  // using LinkedBlockingQueue to avoid manual locks for thread-safe operations
  private final Queue<MetricData> finishedMetricItems = new LinkedBlockingQueue<>();
  private boolean isStopped = false;

  private InMemoryMetricExporter() {}

  /**
   * Returns a new instance of the {@code InMemoryMetricExporter}.
   *
   * @return a new instance of the {@code InMemoryMetricExporter}.
   */
  public static InMemoryMetricExporter create() {
    return new InMemoryMetricExporter();
  }

  /**
   * Returns a {@code List} of the finished {@code Metric}s, represented by {@code MetricData}.
   *
   * @return a {@code List} of the finished {@code Metric}s.
   */
  public List<MetricData> getFinishedMetricItems() {
    return Collections.unmodifiableList(new ArrayList<>(finishedMetricItems));
  }

  /**
   * Clears the internal {@code List} of finished {@code Metric}s.
   *
   * <p>Does not reset the state of this exporter if already shutdown.
   */
  public void reset() {
    finishedMetricItems.clear();
  }

  /**
   * Exports the collection of {@code Metric}s into the inmemory queue.
   *
   * <p>If this is called after {@code shutdown}, this will return {@code ResultCode.FAILURE}.
   */
  @Override
  public ResultCode export(Collection<MetricData> metrics) {
    if (isStopped) {
      return ResultCode.FAILURE;
    }
    finishedMetricItems.addAll(metrics);
    return ResultCode.SUCCESS;
  }

  /**
   * The InMemory exporter does not batch metrics, so this method will immediately return with
   * success.
   *
   * @return always Success
   */
  @Override
  public ResultCode flush() {
    return ResultCode.SUCCESS;
  }

  /**
   * Clears the internal {@code List} of finished {@code Metric}s.
   *
   * <p>Any subsequent call to export() function on this MetricExporter, will return {@code
   * ResultCode.FAILURE}
   */
  @Override
  public void shutdown() {
    isStopped = true;
    finishedMetricItems.clear();
  }
}
