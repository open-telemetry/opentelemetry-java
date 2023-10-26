/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.jdk.internal;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.time.Duration;
import java.util.Collections;
import javax.net.ssl.SSLException;
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
class JdkHttpSenderTest {

  private final HttpClient realHttpClient =
      HttpClient.newBuilder().connectTimeout(Duration.ofMillis(10)).build();
  @Mock private HttpClient mockHttpClient;
  private JdkHttpSender sender;

  @BeforeEach
  void setup() throws IOException, InterruptedException {
    // Can't directly spy on HttpClient for some reason, so create a real instance and a mock that
    // delegates to the real thing
    when(mockHttpClient.send(any(), any()))
        .thenAnswer(
            invocation ->
                realHttpClient.send(invocation.getArgument(0), invocation.getArgument(1)));
    sender =
        new JdkHttpSender(
            mockHttpClient,
            "http://10.255.255.1", // Connecting to a non-routable IP address to trigger connection
            // timeout
            false,
            "text/plain",
            Duration.ofSeconds(10).toNanos(),
            Collections::emptyMap,
            RetryPolicy.builder()
                .setMaxAttempts(2)
                .setInitialBackoff(Duration.ofMillis(1))
                .build());
  }

  @Test
  void sendInternal_RetryableConnectTimeoutException() throws IOException, InterruptedException {
    assertThatThrownBy(() -> sender.sendInternal(marshaler -> {}))
        .isInstanceOf(HttpConnectTimeoutException.class);

    verify(mockHttpClient, times(2)).send(any(), any());
  }

  @Test
  void sendInternal_RetryableIoException() throws IOException, InterruptedException {
    doThrow(new IOException("error!")).when(mockHttpClient).send(any(), any());

    assertThatThrownBy(() -> sender.sendInternal(marshaler -> {}))
        .isInstanceOf(IOException.class)
        .hasMessage("error!");

    verify(mockHttpClient, times(2)).send(any(), any());
  }

  @Test
  void sendInternal_NonRetryableException() throws IOException, InterruptedException {
    doThrow(new SSLException("unknown error")).when(mockHttpClient).send(any(), any());

    assertThatThrownBy(() -> sender.sendInternal(marshaler -> {}))
        .isInstanceOf(IOException.class)
        .hasMessage("unknown error");

    verify(mockHttpClient, times(1)).send(any(), any());
  }

  @Test
  void defaultConnectTimeout() {
    sender =
        new JdkHttpSender(
            "http://localhost", true, "text/plain", 1, Collections::emptyMap, null, null);

    assertThat(sender)
        .extracting("client", as(InstanceOfAssertFactories.type(HttpClient.class)))
        .satisfies(
            httpClient ->
                assertThat(httpClient.connectTimeout().get()).isEqualTo(Duration.ofSeconds(10)));
  }
}
