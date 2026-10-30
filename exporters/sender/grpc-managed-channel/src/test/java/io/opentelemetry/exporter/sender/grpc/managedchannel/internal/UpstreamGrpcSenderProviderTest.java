/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.grpc.managedchannel.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.grpc.ExtendedGrpcSenderConfig;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UpstreamGrpcSenderProviderTest {

  @ParameterizedTest
  @ValueSource(
      strings = {"http://otlp.1234-k8s-namespace:4317", "https://collector.9namespace:4317"})
  void createSender_FallbackChannel_HostUnparseableByUri_Throws(String endpoint) {
    ExtendedGrpcSenderConfig config = config(endpoint);

    assertThatThrownBy(() -> new UpstreamGrpcSenderProvider().createSender(config))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("host not supported by grpc-java: " + endpoint);
  }

  @Test
  void createSender_ExplicitChannel_HostUnparseableByUri_Succeeds() {
    // The endpoint is not used to build an explicitly set channel.
    ExtendedGrpcSenderConfig config = config("http://otlp.1234-k8s-namespace:4317");
    when(config.getManagedChannel()).thenReturn(mock(ManagedChannel.class));

    assertThat(new UpstreamGrpcSenderProvider().createSender(config)).isNotNull();
  }

  private static ExtendedGrpcSenderConfig config(String endpoint) {
    ExtendedGrpcSenderConfig config = mock(ExtendedGrpcSenderConfig.class);
    when(config.getEndpoint()).thenReturn(URI.create(endpoint));
    when(config.getFullMethodName())
        .thenReturn("opentelemetry.proto.collector.trace.v1.TraceService/Export");
    when(config.getTimeout()).thenReturn(Duration.ofSeconds(10));
    when(config.getHeadersSupplier()).thenReturn(Collections::emptyMap);
    return config;
  }
}
