/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.prometheus.client.Collector;
import java.util.Objects;
import javax.annotation.Nullable;

/** Builder for {@link PrometheusCollector}. */
public class PrometheusCollectorBuilder {
  @Nullable private MetricProducer metricProducer;

  PrometheusCollectorBuilder() {}

  /**
   * Sets the metric producer for the collector. Required.
   *
   * @param metricProducer the {@link MetricProducer} to use.
   * @return this builder's instance.
   */
  public PrometheusCollectorBuilder setMetricProducer(MetricProducer metricProducer) {
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
   * Constructs a new instance of the {@link Collector} based on the builder's values and registers
   * it to Prometheus {@link io.prometheus.client.CollectorRegistry#defaultRegistry}.
   *
   * @return a new {@code Collector} based on the builder's values.
   */
  public PrometheusCollector buildAndRegister() {
    return build().register();
  }
}
