/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
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
 * <pre>{@code
 * // class MyClassTest {
 * //   private final InMemoryMetricExporter exporter = InMemoryMetricExporter.create();
 * //   private final SdkMeterProvider meterProvider =
 * //       SdkMeterProvider.builder()
 * //           .registerMetricReader(PeriodicMetricReader.builder(exporter).build())
 * //           .build();
 * //
 * //   @Test
 * //   public void getFinishedMetricItems() {
 * //     LongCounter counter = meterProvider.get("test-scope").counterBuilder("counter").build();
 * //     counter.add(1);
 * //     meterProvider.forceFlush().join(10, TimeUnit.SECONDS);
 * //
 * //     assertThat(exporter.getFinishedMetricItems()).hasSize(1);
 * //   }
 * // }
 * }</pre>
 *
 * @since 1.14.0
 */
public final class InMemoryMetricExporter implements MetricExporter {
  private final Queue<MetricData> finishedMetricItems = new ConcurrentLinkedQueue<>();
  private final AggregationTemporality aggregationTemporality;
  private boolean isStopped = false;

  private InMemoryMetricExporter(AggregationTemporality aggregationTemporality) {
    this.aggregationTemporality = aggregationTemporality;
  }

  /**
   * Returns a new {@link InMemoryMetricExporter} with a aggregation temporality of {@link
   * AggregationTemporality#CUMULATIVE}.
   */
  public static InMemoryMetricExporter create() {
    return create(AggregationTemporality.CUMULATIVE);
  }

  /** Returns a new {@link InMemoryMetricExporter} with the given {@code aggregationTemporality}. */
  public static InMemoryMetricExporter create(AggregationTemporality aggregationTemporality) {
    return new InMemoryMetricExporter(aggregationTemporality);
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
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporality;
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
