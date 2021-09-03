/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.prometheus.client.Collector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class PrometheusCollector extends Collector implements MetricReader {
  private final MetricProducer metricProducer;

  PrometheusCollector(MetricProducer metricProducer) {
    this.metricProducer = metricProducer;
  }

  @Override
  public List<MetricFamilySamples> collect() {
    Collection<MetricData> allMetrics = metricProducer.collectAllMetrics();
    List<MetricFamilySamples> allSamples = new ArrayList<>(allMetrics.size());
    for (MetricData metricData : allMetrics) {
      allSamples.add(MetricAdapter.toMetricFamilySamples(metricData));
    }
    return allSamples;
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static PrometheusCollectorBuilder builder() {
    return new PrometheusCollectorBuilder();
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
}
