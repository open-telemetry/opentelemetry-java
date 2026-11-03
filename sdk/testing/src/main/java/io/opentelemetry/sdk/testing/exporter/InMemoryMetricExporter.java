/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
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
 * <p>Can be created using {@link #create()} or configured using {@link #builder()}.
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
  private final AggregationTemporalitySelector aggregationTemporalitySelector;
  private boolean isStopped = false;

  InMemoryMetricExporter(AggregationTemporalitySelector aggregationTemporalitySelector) {
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
  }

  /**
   * Creates an {@link InMemoryMetricExporterBuilder} with defaults.
   *
   * @return a builder with always-cumulative {@link AggregationTemporalitySelector}
   */
  public static InMemoryMetricExporterBuilder builder() {
    return new InMemoryMetricExporterBuilder();
  }

  /**
   * Returns a new {@link InMemoryMetricExporter} with cumulative aggregation temporality for all
   * instrument types.
   */
  public static InMemoryMetricExporter create() {
    return builder().build();
  }

  /** Returns a new {@link InMemoryMetricExporter} with the given {@code aggregationTemporality}. */
  public static InMemoryMetricExporter create(AggregationTemporality aggregationTemporality) {
    return builder().setAggregationTemporalitySelector(unused -> aggregationTemporality).build();
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
    return aggregationTemporalitySelector.getAggregationTemporality(instrumentType);
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
