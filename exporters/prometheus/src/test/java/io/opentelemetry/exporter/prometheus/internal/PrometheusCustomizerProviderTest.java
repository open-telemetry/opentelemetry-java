/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PrometheusCustomizerProviderTest {

  private static final PrometheusCustomizerProvider provider = new PrometheusCustomizerProvider();

  private SdkMeterProviderBuilder meterProviderBuilder;

  @Mock private ConfigProperties configProperties;

  @Mock private AutoConfigurationCustomizer customizer;

  @BeforeEach
  void setup() {
    meterProviderBuilder = SdkMeterProvider.builder();
    doAnswer(
            invocation -> {
              BiFunction<SdkMeterProviderBuilder, ConfigProperties, SdkMeterProviderBuilder>
                  meterProviderCustomizer = invocation.getArgument(0);
              meterProviderBuilder =
                  meterProviderCustomizer.apply(meterProviderBuilder, configProperties);
              return null;
            })
        .when(customizer)
        .addMeterProviderCustomizer(any());
  }

  @Test
  void customize_PrometheusEnabled() {
    when(configProperties.getList("otel.metrics.exporter"))
        .thenReturn(Collections.singletonList("prometheus"));
    provider.customize(customizer);

    try (SdkMeterProvider meterProvider = meterProviderBuilder.build()) {
      assertThat(meterProvider)
          .extracting("registeredReaders", as(InstanceOfAssertFactories.list(Object.class)))
          .satisfiesExactly(
              registeredReader ->
                  assertThat(registeredReader)
                      .extracting("metricReader")
                      .isInstanceOf(PrometheusHttpServer.class));
    }
  }

  @Test
  void customize_PrometheusDisabled() {
    when(configProperties.getList("otel.metrics.exporter"))
        .thenReturn(Collections.singletonList("foo"));
    provider.customize(customizer);

    try (SdkMeterProvider meterProvider = meterProviderBuilder.build()) {
      assertThat(meterProvider)
          .extracting("registeredReaders", as(InstanceOfAssertFactories.list(Object.class)))
          .isEmpty();
    }
  }

  @Test
  void configurePrometheusHttpServer_Default() {
    try (PrometheusHttpServer prometheusHttpServer =
        PrometheusCustomizerProvider.configurePrometheusHttpServer(
            DefaultConfigProperties.createForTest(Collections.emptyMap()))) {
      assertThat(prometheusHttpServer)
          .extracting("server", as(InstanceOfAssertFactories.type(HttpServer.class)))
          .satisfies(
              server -> {
                assertThat(server.getAddress().getHostName()).isEqualTo("0:0:0:0:0:0:0:0");
                assertThat(server.getAddress().getPort()).isEqualTo(9464);
              });
    }
  }

  @Test
  void configurePrometheusHttpServer_WithConfiguration() throws IOException {
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

    try (PrometheusHttpServer prometheusHttpServer =
        PrometheusCustomizerProvider.configurePrometheusHttpServer(
            DefaultConfigProperties.createForTest(config))) {
      assertThat(prometheusHttpServer)
          .extracting("server", as(InstanceOfAssertFactories.type(HttpServer.class)))
          .satisfies(
              server -> {
                assertThat(server.getAddress().getHostName()).isEqualTo("localhost");
                assertThat(server.getAddress().getPort()).isEqualTo(port);
              });
    }
  }
}
