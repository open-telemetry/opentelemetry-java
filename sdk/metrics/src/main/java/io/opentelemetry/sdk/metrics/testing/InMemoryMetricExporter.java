/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.testing;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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
 *   LongCounter generateLongCounterMeter(String name) {
 *     return meter.longCounterBuilder(name).setDescription("Sample LongCounter").build();
 *   }
 *
 *   public static void main(String[] args) throws InterruptedException {
 *     InMemoryMetricExporterExample example = new InMemoryMetricExporterExample();
 *     example.setup();
 *     example.generateLongCounterMeter("counter-1");
 *   }
 * }
 * </code></pre>
 */
public final class InMemoryMetricExporter implements MetricExporter {
  private final Queue<MetricData> finishedMetricItems = new ConcurrentLinkedQueue<>();
  private final AggregationTemporality preferredTemporality;
  private boolean isStopped = false;

  private InMemoryMetricExporter(AggregationTemporality preferredTemporality) {
    this.preferredTemporality = preferredTemporality;
  }

  /**
   * Returns a new {@link InMemoryMetricExporter} with a preferred temporality of {@link
   * AggregationTemporality#CUMULATIVE}.
   */
  public static InMemoryMetricExporter create() {
    return create(AggregationTemporality.CUMULATIVE);
  }

  /** Returns a new {@link InMemoryMetricExporter} with the given {@code preferredTemporality}. */
  public static InMemoryMetricExporter create(AggregationTemporality preferredTemporality) {
    return new InMemoryMetricExporter(preferredTemporality);
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

  @Override
  public AggregationTemporality getPreferredTemporality() {
    return preferredTemporality;
  }

  /**
   * Exports the collection of {@code Metric}s into the inmemory queue.
   *
   * <p>If this is called after {@code shutdown}, this will return {@code ResultCode.FAILURE}.
   */
  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    if (isStopped) {
      return CompletableResultCode.ofFailure();
    }
    finishedMetricItems.addAll(metrics);
    return CompletableResultCode.ofSuccess();
  }

  /**
   * The InMemory exporter does not batch metrics, so this method will immediately return with
   * success.
   *
   * @return always Success
   */
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Clears the internal {@code List} of finished {@code Metric}s.
   *
   * <p>Any subsequent call to export() function on this MetricExporter, will return {@code
   * CompletableResultCode.ofFailure()}
   */
  @Override
  public CompletableResultCode shutdown() {
    isStopped = true;
    finishedMetricItems.clear();
    return CompletableResultCode.ofSuccess();
  }
}
