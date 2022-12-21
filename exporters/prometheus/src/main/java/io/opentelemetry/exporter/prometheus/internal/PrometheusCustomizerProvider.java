/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServerBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;

/**
 * SPI implementation for {@link PrometheusHttpServer}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class PrometheusCustomizerProvider implements AutoConfigurationCustomizerProvider {

  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addMeterProviderCustomizer(
        (builder, config) -> {
          boolean prometheusEnabled =
              config.getList("otel.metrics.exporter").contains("prometheus");
          if (prometheusEnabled) {
            builder.registerMetricReader(configurePrometheusHttpServer(config));
          }
          return builder;
        });
  }

  // Visible for test
  static PrometheusHttpServer configurePrometheusHttpServer(ConfigProperties config) {
    PrometheusHttpServerBuilder prometheusBuilder = PrometheusHttpServer.builder();

    Integer port = config.getInt("otel.exporter.prometheus.port");
    if (port != null) {
      prometheusBuilder.setPort(port);
    }
    String host = config.getString("otel.exporter.prometheus.host");
    if (host != null) {
      prometheusBuilder.setHost(host);
    }
    return prometheusBuilder.build();
  }
}
