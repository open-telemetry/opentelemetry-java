/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.AbstractMetricReader;
import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * A reader of OpenTelemetry metrics that exports into Prometheus as a Collector.
 *
 * <p>Usage: <code>sdkMeterProvider.registerMetricReader(PrometheusCollector.create());</code>
 *
 * @deprecated This class was intended to fill OpenTelemetry metrics into a Prometheus registry.
 *     Instead, use {@link PrometheusHttpServer} to expose OpenTelemetry metrics as a Prometheus
 *     HTTP endpoint. If you generate metrics with Micrometer in addition to OpenTelemetry, also use
 *     <a
 *     href="https://github.com/open-telemetry/opentelemetry-java-instrumentation/tree/main/instrumentation/micrometer/micrometer-1.5/library">
 *     Micrometer OpenTelemetry Integration</a> to export Micrometer metrics together. If your use
 *     case is not handled by these, please file an issue to let us know.
 */
@Deprecated
public final class PrometheusCollector extends AbstractMetricReader implements MetricReader {

  private final OpenTelemetryCollector openTelemetryCollector;

  PrometheusCollector() {
    this.openTelemetryCollector =
        new OpenTelemetryCollector(() -> getMetricProducer().collectAllMetrics());
    this.openTelemetryCollector.register();
  }

  /**
   * Returns a new collector to be registered with a {@link
   * io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder}.
   */
  public static MetricReader create() {
    return new PrometheusCollector();
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
    CollectorRegistry.defaultRegistry.unregister(openTelemetryCollector);
    return CompletableResultCode.ofSuccess();
  }

  private static class OpenTelemetryCollector extends Collector {

    private final Supplier<Collection<MetricData>> metricSupplier;

    private OpenTelemetryCollector(Supplier<Collection<MetricData>> metricSupplier) {
      this.metricSupplier = metricSupplier;
    }

    @Override
    public List<MetricFamilySamples> collect() {
      Collection<MetricData> allMetrics = metricSupplier.get();
      List<MetricFamilySamples> allSamples = new ArrayList<>(allMetrics.size());
      for (MetricData metricData : allMetrics) {
        allSamples.add(MetricAdapter.toMetricFamilySamples(metricData));
      }
      return Collections.unmodifiableList(allSamples);
    }
  }
}
