/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.testing;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
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
 *   private final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().build();
 *   private final InMemoryMetricReader reader = InMemoryMetricReader.create(sdkMeterProvider);
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
public class InMemoryMetricReader
    implements MetricReader, MetricReader.Factory<InMemoryMetricReader> {
  private volatile MetricProducer sdkCollection;
  private volatile Collection<MetricData> latest = Collections.emptyList();

  public InMemoryMetricReader() {
    // Need to wait for registration for initial value.
  }

  /** Returns all metrics accumulated since the last call. */
  public Collection<MetricData> collectAllMetrics() {
    flush();
    return latest;
  }

  @Override
  public CompletableResultCode flush() {
    if (sdkCollection != null) {
      latest = sdkCollection.collectAllMetrics();
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public InMemoryMetricReader apply(MetricProducer producer) {
    this.sdkCollection = producer;
    return this;
  }
}
