/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.testing;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
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
 * <p> Example usage:
 * 
 * <pre><code>{@code
 * public class InMemoryMetricReaderExample {
 *   private final SdkMeterProvider sdkMeterProvider = SdkMeterProvider.builder().build();
 *   private final InMemoryMetricReader reader = InMemoryMetricReader.create(sdkMeterProvider);
 *   private final Meter meter = sdkMeterProvider.get("example");
 *   private final LongCounter metricCallCount = meter.counterBuilder("num_collects");
 * 
 *   public Collection<MetricData> getMetrics() {
 *     metricCallCount.add(1);
 *     return reader.collectAllMetrics();
 *   }
 * 
 *   public static void main(String[] args) {
 *     InMemoryMetricReaderExample example = new InMemoryMetricReaderExample();
 *     System.out.println(example.getMetrics());
 *   }
 * }
 * }</code></pre>
 */
public class InMemoryMetricReader implements MetricReader {
  private final MetricProducer sdkCollection;
  private volatile Collection<MetricData> latest = Collections.emptyList();

  private InMemoryMetricReader(MetricProducer producer) {
    this.sdkCollection = producer;
  }

  /** 
   * Returns all metrics accumulated since the last call.
   */
  public Collection<MetricData> collectAllMetrics() {
    flush();
    return latest;
  }

  @Override
  public CompletableResultCode flush() {
    latest = sdkCollection.collectAllMetrics();
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }

  static final MetricReader.Factory<InMemoryMetricReader> FACTORY = InMemoryMetricReader::new;

  /**
   * Constructs and in-memory reader and registers it with the specific SDK.
   */
  public static InMemoryMetricReader create(SdkMeterProvider provider) {
    return provider.register(FACTORY);
  }
}
