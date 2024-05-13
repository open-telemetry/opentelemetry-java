/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrometheusMetricReaderProviderTest {

  private static final PrometheusMetricReaderProvider provider =
      new PrometheusMetricReaderProvider();

  @Mock private ConfigProperties configProperties;

  @Test
  void getName() {
    assertThat(provider.getName()).isEqualTo("prometheus");
  }

  @Test
  void createMetricReader_Default() throws IOException {
    when(configProperties.getInt(any())).thenReturn(null);
    when(configProperties.getString(any())).thenReturn(null);

    try (MetricReader metricReader = provider.createMetricReader(configProperties)) {
      assertThat(metricReader)
          .isInstanceOf(PrometheusHttpServer.class)
          .extracting("httpServer", as(InstanceOfAssertFactories.type(HTTPServer.class)))
          .extracting("server", as(InstanceOfAssertFactories.type(HttpServer.class)))
          .satisfies(
              server -> {
                assertThat(server.getAddress().getHostName()).isEqualTo("0:0:0:0:0:0:0:0");
                assertThat(server.getAddress().getPort()).isEqualTo(9464);
              });
      assertThat(metricReader.getMemoryMode()).isEqualTo(MemoryMode.IMMUTABLE_DATA);
    }
  }

  @Test
  void createMetricReader_WithConfiguration() throws IOException {
    // Find a random unused port. There's a small race if another process takes it before we
    // initialize. Consider adding retries to this test if it flakes, presumably it never will on
    // CI since there's no prometheus there blocking the well-known port.
    int port;
    try (ServerSocket socket2 = new ServerSocket(0)) {
      port = socket2.getLocalPort();
    }

    Map<String, String> config = new HashMap<>();
    config.put("otel.exporter.prometheus.host", "localhost");
    config.put("otel.exporter.prometheus.port", String.valueOf(port));
    config.put("otel.java.experimental.exporter.memory_mode", "reusable_data");

    when(configProperties.getInt(any())).thenReturn(null);
    when(configProperties.getString(any())).thenReturn(null);

    try (MetricReader metricReader =
        provider.createMetricReader(DefaultConfigProperties.createFromMap(config))) {
      assertThat(metricReader)
          .extracting("httpServer", as(InstanceOfAssertFactories.type(HTTPServer.class)))
          .extracting("server", as(InstanceOfAssertFactories.type(HttpServer.class)))
          .satisfies(
              server -> {
                assertThat(server.getAddress().getHostName()).isEqualTo("localhost");
                assertThat(server.getAddress().getPort()).isEqualTo(port);
              });
      assertThat(metricReader.getMemoryMode()).isEqualTo(MemoryMode.REUSABLE_DATA);
    }
  }
}
