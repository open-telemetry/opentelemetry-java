/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.prometheus.client.Collector;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class PrometheusCollector extends Collector {
  private final MetricProducer metricProducer;

  private PrometheusCollector(MetricProducer metricProducer) {
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
  public static Builder builder() {
    return new Builder();
  }

  /** Builder utility for this exporter. */
  public static class Builder {
    private MetricProducer metricProducer;

    /**
     * Sets the metric producer for the collector. Required.
     *
     * @param metricProducer the {@link MetricProducer} to use.
     * @return this builder's instance.
     */
    public Builder setMetricProducer(MetricProducer metricProducer) {
      this.metricProducer = metricProducer;
      return this;
    }

    /**
     * Constructs a new instance of the {@link Collector} based on the builder's values.
     *
     * @return a new {@code Collector} based on the builder's values.
     */
    public PrometheusCollector build() {
      return new PrometheusCollector(Objects.requireNonNull(metricProducer, "metricProducer"));
    }

    /**
     * Constructs a new instance of the {@link Collector} based on the builder's values and
     * registers it to Prometheus {@link io.prometheus.client.CollectorRegistry#defaultRegistry}.
     *
     * @return a new {@code Collector} based on the builder's values.
     */
    public PrometheusCollector buildAndRegister() {
      return build().register();
    }
  }
}
