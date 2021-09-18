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
import java.util.Collections;
import java.util.List;

public final class PrometheusCollector extends Collector
    implements MetricReader, MetricReader.Factory {
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
