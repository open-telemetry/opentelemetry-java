/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.example.prometheus;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.prometheus.PrometheusCollector;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.prometheus.client.exporter.HTTPServer;
import java.io.IOException;

public class ExampleConfiguration {
  private static HTTPServer server;

  /**
   * Initializes the Meter SDK and configures the prometheus collector with all default settings.
   *
   * @param prometheusPort the port to open up for scraping.
   * @return A MeterProvider for use in instrumentation.
   */
  static MeterProvider initializeOpenTelemetry(int prometheusPort) throws IOException {
    SdkMeterProvider meterProvider = SdkMeterProvider.builder().buildAndRegisterGlobal();

    PrometheusCollector.builder().setMetricProducer(meterProvider).buildAndRegister();

    server = new HTTPServer(prometheusPort);

    return meterProvider;
  }

  static void shutdownPrometheusEndpoint() {
    server.stop();
  }
}
