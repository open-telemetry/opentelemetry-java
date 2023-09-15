/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.MultiMetricProducerReader;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A {@link MetricReader} implementation that can be used to test OpenTelemetry integration.
 *
 * <p>Can be created using {@code InMemoryMetricReader.create()}
 *
 * <p>Example usage:
 *
 * <pre><code>
 * public class InMemoryMetricReaderExample {
 *   private final InMemoryMetricReader reader = InMemoryMetricReader.create();
 *   private final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().registerMetricReader(reader).build();
 *   private final Meter meter = sdkMeterProvider.get("example");
 *   private final LongCounter metricCallCount = meter.counterBuilder("num_collects");
 *
 *   public void printMetrics() {
 *     metricCallCount.add(1);
 *     System.out.println(reader.collectAllMetrics());
 *   }
 *
 *   public static void main(String[] args) {
 *     InMemoryMetricReaderExample example = new InMemoryMetricReaderExample();
 *     example.printMetrics();
 *   }
 * }
 * </code></pre>
 *
 * @since 1.14.0
 */
public class InMemoryMetricReader implements MetricReader {
  private final AggregationTemporalitySelector aggregationTemporalitySelector;
  private final DefaultAggregationSelector defaultAggregationSelector;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);
  private final MultiMetricProducerReader multiMetricProducerReader =
      MultiMetricProducerReader.create();

  /** Returns a new {@link InMemoryMetricReader}. */
  public static InMemoryMetricReader create() {
    return new InMemoryMetricReader(
        AggregationTemporalitySelector.alwaysCumulative(), DefaultAggregationSelector.getDefault());
  }

  /**
   * Returns a new {@link InMemoryMetricReader}.
   *
   * @since 1.26.0
   */
  public static InMemoryMetricReader create(
      AggregationTemporalitySelector aggregationTemporalitySelector,
      DefaultAggregationSelector defaultAggregationSelector) {
    return new InMemoryMetricReader(aggregationTemporalitySelector, defaultAggregationSelector);
  }

  /** Creates a new {@link InMemoryMetricReader} that prefers DELTA aggregation. */
  public static InMemoryMetricReader createDelta() {
    return new InMemoryMetricReader(
        unused -> AggregationTemporality.DELTA, DefaultAggregationSelector.getDefault());
  }

  private InMemoryMetricReader(
      AggregationTemporalitySelector aggregationTemporalitySelector,
      DefaultAggregationSelector defaultAggregationSelector) {
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    this.defaultAggregationSelector = defaultAggregationSelector;
  }

  /** Returns all metrics accumulated since the last call. */
  public Collection<MetricData> collectAllMetrics() {
    if (isShutdown.get()) {
      return Collections.emptyList();
    }
    return multiMetricProducerReader.readAll();
  }

  @Override
  public void register(CollectionRegistration collectionRegistration) {
    multiMetricProducerReader.register(collectionRegistration);
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporalitySelector.getAggregationTemporality(instrumentType);
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return defaultAggregationSelector.getDefaultAggregation(instrumentType);
  }

  @Override
  public CompletableResultCode forceFlush() {
    collectAllMetrics();
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    isShutdown.set(true);
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    return "InMemoryMetricReader{}";
  }
}
