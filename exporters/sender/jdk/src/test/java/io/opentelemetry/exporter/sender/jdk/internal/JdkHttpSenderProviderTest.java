/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.jdk.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.export.HttpSender;
import io.opentelemetry.sdk.common.export.HttpSenderConfig;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JdkHttpSenderProviderTest {

  @ParameterizedTest
  @ValueSource(
      strings = {
        "http://otlp.1234-k8s-namespace:4318/v1/traces",
        "https://collector.9namespace:4318/v1/traces",
      })
  void createSender_HostUnparseableByUri_Throws(String endpoint) {
    HttpSenderConfig config = config(endpoint);

    assertThatThrownBy(() -> new JdkHttpSenderProvider().createSender(config))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("host not supported by the JDK HttpClient: " + endpoint);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "http://localhost:4318/v1/traces",
        "http://otlp.1234-k8s-namespace.svc.cluster.local:4318/v1/traces",
      })
  void createSender_HostParseableByUri_Succeeds(String endpoint) {
    HttpSender sender = new JdkHttpSenderProvider().createSender(config(endpoint));

    assertThat(sender.shutdown().join(10, TimeUnit.SECONDS).isSuccess()).isTrue();
  }

  private static HttpSenderConfig config(String endpoint) {
    HttpSenderConfig config = mock(HttpSenderConfig.class);
    when(config.getEndpoint()).thenReturn(URI.create(endpoint));
    when(config.getContentType()).thenReturn("application/x-protobuf");
    when(config.getTimeout()).thenReturn(Duration.ofSeconds(10));
    when(config.getConnectTimeout()).thenReturn(Duration.ofSeconds(10));
    when(config.getHeadersSupplier()).thenReturn(Collections::emptyMap);
    when(config.getMaxResponseBodySize()).thenReturn(Long.MAX_VALUE);
    return config;
  }
}
