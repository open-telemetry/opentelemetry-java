/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.testing;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import java.util.Collection;
import java.util.Collections;

/**
 * A {@link MetricReader} implementation that can be used to test OpenTelemetry integration.
 *
 * <p>Can be created using {@code InMemoryMetricReader.create(sdkMeterProvider)}
 *
 * <p>Example usage:
 *
 * <pre><code>
 * public class InMemoryMetricReaderExample {
 *   private final InMemoryMetricReader reader = new InMemoryMetricReader();
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
 */
public class InMemoryMetricReader implements MetricReader, MetricReaderFactory {
  private volatile MetricProducer metricProducer;

  /** Returns all metrics accumulated since the last call. */
  public Collection<MetricData> collectAllMetrics() {
    MetricProducer metricProducer = this.metricProducer;
    if (metricProducer != null) {
      return metricProducer.collectAllMetrics();
    }
    return Collections.emptyList();
  }

  @Override
  public CompletableResultCode flush() {
    MetricProducer metricProducer = this.metricProducer;
    if (metricProducer != null) {
      metricProducer.collectAllMetrics();
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public MetricReader apply(MetricProducer producer) {
    this.metricProducer = producer;
    return this;
  }
}
