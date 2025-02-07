/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrometheusMetricReaderProviderTest {

  private static final PrometheusMetricReaderProvider provider =
      new PrometheusMetricReaderProvider();

  @Test
  void getName() {
    assertThat(provider.getName()).isEqualTo("prometheus");
  }

  @Test
  void createMetricReader_Default() throws IOException {
    Map<String, String> config = new HashMap<>();
    // Although this test aims to test the defaults, the default port of 9464 may produce a port
    // conflict error. Therefore, we set the port to 0, allowing an available port to be
    // automatically assigned.
    config.put("otel.exporter.prometheus.port", "0");

    try (MetricReader metricReader =
        provider.createMetricReader(DefaultConfigProperties.createFromMap(config))) {
      assertThat(metricReader)
          .isInstanceOf(PrometheusHttpServer.class)
          .extracting("httpServer", as(InstanceOfAssertFactories.type(HTTPServer.class)))
          .extracting("server", as(InstanceOfAssertFactories.type(HttpServer.class)))
          .satisfies(
              server -> {
                assertThat(server.getAddress().getHostName()).isEqualTo("0:0:0:0:0:0:0:0");
                assertThat(server.getAddress().getPort()).isPositive();
              });
      assertThat(metricReader.getMemoryMode()).isEqualTo(MemoryMode.REUSABLE_DATA);
      assertThat(metricReader.getDefaultAggregation(InstrumentType.HISTOGRAM))
          .isEqualTo(Aggregation.defaultAggregation());
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
    config.put("otel.java.exporter.memory_mode", "immutable_data");
    config.put(
        "otel.java.experimental.exporter.prometheus.metrics.default.histogram.aggregation",
        "BASE2_EXPONENTIAL_BUCKET_HISTOGRAM");

    try (MetricReader metricReader =
        provider.createMetricReader(DefaultConfigProperties.createFromMap(config))) {
      assertThat(metricReader)
          .extracting("httpServer", as(InstanceOfAssertFactories.type(HTTPServer.class)))
          .extracting("server", as(InstanceOfAssertFactories.type(HttpServer.class)))
          .satisfies(
              server -> {
                assertThat(server.getAddress().getHostName())
                    .isIn("localhost", "127.0.0.1", "kubernetes.docker.internal");
                assertThat(server.getAddress().getPort()).isEqualTo(port);
              });
      assertThat(metricReader.getMemoryMode()).isEqualTo(MemoryMode.IMMUTABLE_DATA);
      assertThat(metricReader.getDefaultAggregation(InstrumentType.HISTOGRAM))
          .isEqualTo(Aggregation.base2ExponentialBucketHistogram());
    }
  }

  @Test
  void createMetricReader_WithWrongConfiguration() {
    Map<String, String> config = new HashMap<>();
    config.put(
        "otel.java.experimental.exporter.prometheus.metrics.default.histogram.aggregation", "foo");

    assertThatThrownBy(
            () -> provider.createMetricReader(DefaultConfigProperties.createFromMap(config)))
        .isInstanceOf(ConfigurationException.class)
        .hasMessageContaining("Unrecognized default histogram aggregation:");
  }
}
