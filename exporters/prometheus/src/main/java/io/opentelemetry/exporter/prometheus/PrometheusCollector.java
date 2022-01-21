/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A reader of OpenTelemetry metrics that exports into Prometheus as a Collector.
 *
 * <p>Usage: <code>sdkMeterProvider.registerMetricReader(PrometheusCollector.create());</code>
 */
public final class PrometheusCollector extends Collector implements MetricReader {
  private final MetricProducer metricProducer;
  private volatile boolean registered = false;

  PrometheusCollector(MetricProducer metricProducer) {
    this.metricProducer = metricProducer;
  }

  /**
   * This method is called in {@link Factory#apply(MetricProducer)}. {@link Collector#register()}
   * triggers a call to {@link #collect()}, which throws an error because {@link
   * MetricProducer#collectAllMetrics()} is not yet read to accept calls. To get around this, we
   * have {@link #collect()} exit early until registration is complete.
   */
  @SuppressWarnings("TypeParameterUnusedInFormals")
  @Override
  public <T extends Collector> T register() {
    T result = super.register();
    this.registered = true;
    return result;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    if (!registered) {
      return Collections.emptyList();
    }
    Collection<MetricData> allMetrics = metricProducer.collectAllMetrics();
    List<MetricFamilySamples> allSamples = new ArrayList<>(allMetrics.size());
    for (MetricData metricData : allMetrics) {
      allSamples.add(MetricAdapter.toMetricFamilySamples(metricData));
    }
    return Collections.unmodifiableList(allSamples);
  }

  /**
   * Returns a new collector to be registered with a {@link
   * io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder}.
   */
  public static MetricReaderFactory create() {
    return new Factory();
  }

  @Override
  public AggregationTemporality getPreferredTemporality() {
    return AggregationTemporality.CUMULATIVE;
  }

  // Prometheus cannot flush.
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    CollectorRegistry.defaultRegistry.unregister(this);
    return CompletableResultCode.ofSuccess();
  }

  /** Our implementation of the metric reader factory. */
  // NOTE: This should be updated to (optionally) start the simple Http server exposing the metrics
  // path.
  private static class Factory implements MetricReaderFactory {
    @Override
    public MetricReader apply(MetricProducer producer) {
      PrometheusCollector collector = new PrometheusCollector(producer);
      // When SdkMeterProvider constructs us, we register with prometheus.
      collector.register();
      return collector;
    }
  }
}
