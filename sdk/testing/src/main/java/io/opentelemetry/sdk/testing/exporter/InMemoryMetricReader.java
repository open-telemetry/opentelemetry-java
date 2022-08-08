/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.exporter;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;
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
  private final AggregationTemporality aggregationTemporality;
  private final AtomicBoolean isShutdown = new AtomicBoolean(false);
  private volatile MetricProducer metricProducer = MetricProducer.noop();

  /** Returns a new {@link InMemoryMetricReader}. */
  public static InMemoryMetricReader create() {
    return new InMemoryMetricReader(AggregationTemporality.CUMULATIVE);
  }

  /** Creates a new {@link InMemoryMetricReader} that prefers DELTA aggregation. */
  public static InMemoryMetricReader createDelta() {
    return new InMemoryMetricReader(AggregationTemporality.DELTA);
  }

  private InMemoryMetricReader(AggregationTemporality aggregationTemporality) {
    this.aggregationTemporality = aggregationTemporality;
  }

  /** Returns all metrics accumulated since the last call. */
  public Collection<MetricData> collectAllMetrics() {
    if (isShutdown.get()) {
      return Collections.emptyList();
    }
    return metricProducer.collectAllMetrics();
  }

  @Override
  public void register(CollectionRegistration registration) {
    this.metricProducer = MetricProducer.asMetricProducer(registration);
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporality;
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
    return "InMemoryMetricReader{aggregationTemporality=" + aggregationTemporality + "}";
  }
}
