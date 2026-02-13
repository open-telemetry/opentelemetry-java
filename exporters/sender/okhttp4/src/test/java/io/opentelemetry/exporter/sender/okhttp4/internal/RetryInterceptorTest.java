/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp4.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpRetryException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

@ExtendWith(MockitoExtension.class)
class RetryInterceptorTest {

  @RegisterExtension static final MockWebServerExtension server = new MockWebServerExtension();

  @Mock private RetryInterceptor.Sleeper sleeper;
  @Mock private Supplier<Double> random;
  private Predicate<IOException> retryExceptionPredicate;

  private RetryInterceptor retrier;
  private OkHttpClient client;

  @BeforeEach
  void setUp() {
    Logger logger = Logger.getLogger(RetryInterceptor.class.getName());
    logger.setLevel(Level.FINER);
    retryExceptionPredicate =
        spy(
            new Predicate<IOException>() {
              @Override
              public boolean test(IOException e) {
                return RetryInterceptor.isRetryableException(e)
                    || (e instanceof HttpRetryException
                        && e.getMessage().contains("timeout retry"));
              }
            });

    RetryPolicy retryPolicy =
        RetryPolicy.builder()
            .setBackoffMultiplier(1.6)
            .setInitialBackoff(Duration.ofSeconds(1))
            .setMaxBackoff(Duration.ofSeconds(2))
            .setMaxAttempts(5)
            .setRetryExceptionPredicate(retryExceptionPredicate)
            .build();

    retrier =
        new RetryInterceptor(
            retryPolicy, r -> !r.isSuccessful(), retryExceptionPredicate, sleeper, random);
    client = new OkHttpClient.Builder().addInterceptor(retrier).build();
  }

  @Test
  void noRetryOnNullResponse() throws IOException {
    Interceptor.Chain chain = mock(Interceptor.Chain.class);
    when(chain.proceed(any())).thenReturn(null);
    when(chain.request())
        .thenReturn(new Request.Builder().url(server.httpUri().toString()).build());
    assertThatThrownBy(
            () -> {
              retrier.intercept(chain);
            })
        .isInstanceOf(NullPointerException.class)
        .hasMessage("response cannot be null.");

    verifyNoInteractions(retryExceptionPredicate);
    verifyNoInteractions(random);
    verifyNoInteractions(sleeper);
  }

  @Test
  void noRetry() throws Exception {
    server.enqueue(HttpResponse.of(HttpStatus.OK));

    try (Response response = sendRequest()) {
      assertThat(response.isSuccessful()).isTrue();
    }

    verifyNoInteractions(random);
    verifyNoInteractions(sleeper);
  }

  @ParameterizedTest
  // Test is mostly same for 5 or more attempts since it's the max. We check the backoff timings and
  // handling of max attempts by checking both.
  @ValueSource(ints = {5, 6})
  void backsOff(int attempts) throws Exception {
    succeedOnAttempt(attempts);
    when(random.get()).thenReturn(1.0d);
    doNothing().when(sleeper).sleep(anyLong());

    try (Response response = sendRequest()) {
      if (attempts <= 5) {
        assertThat(response.isSuccessful()).isTrue();
      } else {
        assertThat(response.isSuccessful()).isFalse();
      }
    }

    for (int i = 0; i < 5; i++) {
      server.takeRequest(0, TimeUnit.NANOSECONDS);
    }
  }

  @Test
  void interrupted() throws Exception {
    succeedOnAttempt(5);

    // Backs off twice, second is interrupted
    when(random.get()).thenReturn(1.0d).thenReturn(1.0d);
    doAnswer(
            new Answer<Void>() {
              int counter = 0;

              @Override
              public Void answer(InvocationOnMock invocation) throws Throwable {
                if (counter++ == 1) {
                  throw new InterruptedException();
                }
                return null;
              }
            })
        .when(sleeper)
        .sleep(anyLong());

    try (Response response = sendRequest()) {
      assertThat(response.isSuccessful()).isFalse();
    }
    verify(sleeper, times(2)).sleep(anyLong());
    for (int i = 0; i < 2; i++) {
      server.takeRequest(0, TimeUnit.NANOSECONDS);
    }
  }

  @Test
  void connectTimeout() throws Exception {
    client = connectTimeoutClient();
    when(random.get()).thenReturn(1.0d);
    doNothing().when(sleeper).sleep(anyLong());

    // Connecting to a non-routable IP address to trigger connection error
    assertThatThrownBy(
            () ->
                client.newCall(new Request.Builder().url("http://10.255.255.1").build()).execute())
        .isInstanceOfAny(SocketTimeoutException.class, SocketException.class);

    verify(retryExceptionPredicate, times(5)).test(any());
    // Should retry maxAttempts, and sleep maxAttempts - 1 times
    verify(sleeper, times(4)).sleep(anyLong());
  }

  @Test
  void connectException() throws Exception {
    client = connectTimeoutClient();
    when(random.get()).thenReturn(1.0d);
    doNothing().when(sleeper).sleep(anyLong());

    // Connecting to localhost on an unused port address to trigger java.net.ConnectException
    int openPort = freePort();
    assertThatThrownBy(
            () ->
                client
                    .newCall(new Request.Builder().url("http://localhost:" + openPort).build())
                    .execute())
        .isInstanceOfAny(ConnectException.class, SocketTimeoutException.class);

    verify(retryExceptionPredicate, times(5)).test(any());
    // Should retry maxAttempts, and sleep maxAttempts - 1 times
    verify(sleeper, times(4)).sleep(anyLong());
  }

  private static int freePort() {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Test
  void nonRetryableException() throws InterruptedException {
    client = connectTimeoutClient();
    // Override retryPredicate so that no exception is retryable
    when(retryExceptionPredicate.test(any())).thenReturn(false);

    // Connecting to a non-routable IP address to trigger connection timeout
    assertThatThrownBy(
            () ->
                client.newCall(new Request.Builder().url("http://10.255.255.1").build()).execute())
        .isInstanceOfAny(SocketTimeoutException.class, SocketException.class);

    verify(retryExceptionPredicate, times(1)).test(any());
    verify(sleeper, never()).sleep(anyLong());
  }

  private OkHttpClient connectTimeoutClient() {
    return new OkHttpClient.Builder()
        .connectTimeout(Duration.ofMillis(10))
        .addInterceptor(retrier)
        .build();
  }

  @ParameterizedTest
  @MethodSource("isRetryableExceptionArgs")
  void isRetryableException(IOException exception, boolean expectedRetryResult) {
    assertThat(retrier.shouldRetryOnException(exception)).isEqualTo(expectedRetryResult);
  }

  private static Stream<Arguments> isRetryableExceptionArgs() {
    return Stream.of(
        // Should retry on SocketTimeoutExceptions
        Arguments.of(new SocketTimeoutException("Connect timed out"), true),
        Arguments.of(new SocketTimeoutException("connect timed out"), true),
        Arguments.of(new SocketTimeoutException("timeout"), true),
        Arguments.of(new SocketTimeoutException("Read timed out"), true),
        Arguments.of(new SocketTimeoutException(), true),
        // Should retry on UnknownHostExceptions
        Arguments.of(new UnknownHostException("host"), true),
        // Should retry on SocketException
        Arguments.of(new SocketException("closed"), true),
        // Should retry on ConnectException
        Arguments.of(
            new ConnectException("Failed to connect to localhost/[0:0:0:0:0:0:0:1]:62611"), true),
        // Shouldn't retry other IOException
        Arguments.of(new IOException("error"), false),
        // Testing configured predicate
        Arguments.of(new HttpRetryException("error", 400), false),
        Arguments.of(new HttpRetryException("timeout retry", 400), true));
  }

  @Test
  void isRetryableExceptionDefaultBehaviour() {
    RetryInterceptor retryInterceptor =
        new RetryInterceptor(RetryPolicy.getDefault(), OkHttpHttpSender::isRetryable);
    assertThat(
            retryInterceptor.shouldRetryOnException(
                new SocketTimeoutException("Connect timed out")))
        .isTrue();
    assertThat(retryInterceptor.shouldRetryOnException(new IOException("Connect timed out")))
        .isFalse();
  }

  @Test
  void isRetryableExceptionCustomRetryPredicate() {
    RetryInterceptor retryInterceptor =
        new RetryInterceptor(
            RetryPolicy.builder()
                .setRetryExceptionPredicate((IOException e) -> e.getMessage().equals("retry"))
                .build(),
            OkHttpHttpSender::isRetryable);

    assertThat(retryInterceptor.shouldRetryOnException(new IOException("some message"))).isFalse();
    assertThat(retryInterceptor.shouldRetryOnException(new IOException("retry"))).isTrue();
    assertThat(
            retryInterceptor.shouldRetryOnException(
                new SocketTimeoutException("Connect timed out")))
        .isFalse();
  }

  private Response sendRequest() throws IOException {
    return client.newCall(new Request.Builder().url(server.httpUri().toString()).build()).execute();
  }

  private static void succeedOnAttempt(int attempt) {
    for (int i = 1; i < attempt; i++) {
      server.enqueue(HttpResponse.of(HttpStatus.INTERNAL_SERVER_ERROR));
    }
    server.enqueue(HttpResponse.of(HttpStatus.OK));
  }
}
