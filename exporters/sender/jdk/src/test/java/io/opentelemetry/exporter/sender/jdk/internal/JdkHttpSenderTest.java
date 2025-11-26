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

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.http.HttpClient;
import java.net.http.HttpConnectTimeoutException;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLException;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledForJreRange;
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
            // Connecting to a non-routable IP address to trigger connection timeout
            "http://10.255.255.1",
            null,
            false,
            "text/plain",
            Duration.ofSeconds(10).toNanos(),
            Collections::emptyMap,
            RetryPolicy.builder().setMaxAttempts(2).setInitialBackoff(Duration.ofMillis(1)).build(),
            null);
  }

  @Test
  @EnabledForJreRange(
      minVersion = 21,
      disabledReason = "HttpClient#close has been added in Java 21")
  void testShutdown() throws Exception {
    CompletableResultCode result = sender.shutdown();
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isTrue();
    Method close = HttpClient.class.getMethod("close");
    close.invoke(verify(mockHttpClient));
  }

  @Test
  @EnabledForJreRange(
      minVersion = 21,
      disabledReason = "HttpClient#close has been added in Java 21")
  void testShutdownException() throws Exception {
    Method close = HttpClient.class.getMethod("close");
    close.invoke(doThrow(new RuntimeException("testShutdownException")).when(mockHttpClient));

    CompletableResultCode result = sender.shutdown();
    result.join(1, TimeUnit.SECONDS);
    assertThat(result.isSuccess()).isFalse();
    assertThat(result.getFailureThrowable()).isInstanceOf(RuntimeException.class);
    assertThat(result.getFailureThrowable().getMessage()).isEqualTo("testShutdownException");
  }

  @Test
  void sendInternal_RetryableConnectTimeoutException() throws IOException, InterruptedException {
    assertThatThrownBy(() -> sender.sendInternal(new NoOpMarshaler()))
        .satisfies(
            e ->
                assertThat((e instanceof HttpConnectTimeoutException) || (e instanceof IOException))
                    .isTrue());

    verify(mockHttpClient, times(2)).send(any(), any());
  }

  @Test
  void sendInternal_RetryableConnectException() throws IOException, InterruptedException {
    sender =
        new JdkHttpSender(
            mockHttpClient,
            // Connecting to localhost on an unused port address to trigger
            // java.net.ConnectException (or java.net.http.HttpConnectTimeoutException on linux java
            // 11+)
            "http://localhost:" + freePort(),
            null,
            false,
            "text/plain",
            Duration.ofSeconds(10).toNanos(),
            Collections::emptyMap,
            RetryPolicy.builder().setMaxAttempts(2).setInitialBackoff(Duration.ofMillis(1)).build(),
            null);

    assertThatThrownBy(() -> sender.sendInternal(new NoOpMarshaler()))
        .satisfies(
            e ->
                assertThat(
                        (e instanceof ConnectException)
                            || (e instanceof HttpConnectTimeoutException))
                    .isTrue());

    verify(mockHttpClient, times(2)).send(any(), any());
  }

  private static int freePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void sendInternal_RetryableIoException() throws IOException, InterruptedException {
    doThrow(new IOException("error!")).when(mockHttpClient).send(any(), any());

    assertThatThrownBy(() -> sender.sendInternal(new NoOpMarshaler()))
        .isInstanceOf(IOException.class)
        .hasMessage("error!");

    verify(mockHttpClient, times(2)).send(any(), any());
  }

  @Test
  void sendInternal_NonRetryableException() throws IOException, InterruptedException {
    doThrow(new SSLException("unknown error")).when(mockHttpClient).send(any(), any());

    assertThatThrownBy(() -> sender.sendInternal(new NoOpMarshaler()))
        .isInstanceOf(IOException.class)
        .hasMessage("unknown error");

    verify(mockHttpClient, times(1)).send(any(), any());
  }

  @Test
  void connectTimeout() {
    sender =
        new JdkHttpSender(
            "http://localhost",
            null,
            false,
            "text/plain",
            1,
            TimeUnit.SECONDS.toNanos(10),
            Collections::emptyMap,
            null,
            null,
            null,
            null);

    assertThat(sender)
        .extracting("client", as(InstanceOfAssertFactories.type(HttpClient.class)))
        .satisfies(
            httpClient ->
                assertThat(httpClient.connectTimeout().get()).isEqualTo(Duration.ofSeconds(10)));
  }

  private static class NoOpMarshaler extends Marshaler {

    @Override
    public int getBinarySerializedSize() {
      return 0;
    }

    @Override
    protected void writeTo(Serializer output) {}
  }
}
