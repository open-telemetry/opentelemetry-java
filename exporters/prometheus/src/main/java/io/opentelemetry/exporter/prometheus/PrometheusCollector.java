/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import io.prometheus.client.Collector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A reader of OpenTelemetry metrics that exports into Prometheus as a Collector.
 *
 * <p>Usage: <code>sdkMeterProvider.registerMetricReader(PrometheusCollector.create());</code>
 */
public final class PrometheusCollector extends Collector
    implements MetricReader, MetricReaderFactory {
  // Note: we expect the `apply` method of `MetricReaderFactory` to be called
  // prior to registering this collector with the prometheus client library.
  // This means this field does not need to be volatile because it will
  // be filled out (and no longer mutated) prior to being shared with other threads.
  private MetricProducer metricProducer;

  PrometheusCollector() {}

  @Override
  public List<MetricFamilySamples> collect() {
    if (metricProducer == null) {
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
  public static PrometheusCollector create() {
    return new PrometheusCollector();
  }

  // Prometheus cannot flush.
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    // TODO - Can we unsubscribe ourselves from callbacks?
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public MetricReader apply(MetricProducer producer) {
    this.metricProducer = producer;
    // When SdkMeterProvider constructs us, we register with prometheus.
    register();
    return this;
  }
}
