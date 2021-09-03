/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.prometheus.client.Collector;

/** Builder for {@link PrometheusCollector}. */
public class PrometheusCollectorBuilder {
  private SdkMeterProvider meterProvider;

  PrometheusCollectorBuilder() {}

  /**
   * Sets the meter provider for the collector. Required.
   *
   * @param meterProvider the {@link SdkMeterProvider} to use.
   * @return this builder's instance.
   */
  public PrometheusCollectorBuilder setMeterProvider(SdkMeterProvider meterProvider) {
    this.meterProvider = meterProvider;
    return this;
  }

  /**
   * Constructs a new instance of the {@link Collector} based on the builder's values.
   *
   * @return a new {@code Collector} based on the builder's values.
   */
  public PrometheusCollector build() {
    return meterProvider.register(PrometheusCollector::new);
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
