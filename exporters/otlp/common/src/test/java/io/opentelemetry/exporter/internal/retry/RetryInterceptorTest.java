/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RetryInterceptorTest {

  @RegisterExtension static final MockWebServerExtension server = new MockWebServerExtension();

  @Mock private RetryInterceptor.Sleeper sleeper;
  @Mock private RetryInterceptor.BoundedLongGenerator random;
  private Function<IOException, Boolean> isRetryableException;

  private OkHttpClient client;

  @BeforeEach
  void setUp() {
    // Note: cannot replace this with lambda or method reference because we need to spy on it
    isRetryableException =
        spy(
            new Function<IOException, Boolean>() {
              @Override
              public Boolean apply(IOException exception) {
                return RetryInterceptor.isRetryableException(exception);
              }
            });
    RetryInterceptor retrier =
        new RetryInterceptor(
            RetryPolicy.builder()
                .setBackoffMultiplier(1.6)
                .setInitialBackoff(Duration.ofSeconds(1))
                .setMaxBackoff(Duration.ofSeconds(2))
                .setMaxAttempts(5)
                .build(),
            r -> !r.isSuccessful(),
            isRetryableException,
            sleeper,
            random);
    client =
        new OkHttpClient.Builder()
            .connectTimeout(Duration.ofMillis(10))
            .addInterceptor(retrier)
            .build();
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

    // Will backoff 4 times
    when(random.get((long) (TimeUnit.SECONDS.toNanos(1) * Math.pow(1.6, 0)))).thenReturn(100L);
    when(random.get((long) (TimeUnit.SECONDS.toNanos(1) * Math.pow(1.6, 1)))).thenReturn(50L);
    // Capped
    when(random.get(TimeUnit.SECONDS.toNanos(2))).thenReturn(500L).thenReturn(510L);

    doNothing().when(sleeper).sleep(100);
    doNothing().when(sleeper).sleep(50);
    doNothing().when(sleeper).sleep(500);
    doNothing().when(sleeper).sleep(510);

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
    when(random.get((long) (TimeUnit.SECONDS.toNanos(1) * Math.pow(1.6, 0)))).thenReturn(100L);
    when(random.get((long) (TimeUnit.SECONDS.toNanos(1) * Math.pow(1.6, 1)))).thenReturn(50L);

    doNothing().when(sleeper).sleep(100);
    doThrow(new InterruptedException()).when(sleeper).sleep(50);

    try (Response response = sendRequest()) {
      assertThat(response.isSuccessful()).isFalse();
    }

    for (int i = 0; i < 2; i++) {
      server.takeRequest(0, TimeUnit.NANOSECONDS);
    }
  }

  @Test
  void connectTimeout() throws Exception {
    when(random.get(anyLong())).thenReturn(1L);
    doNothing().when(sleeper).sleep(anyLong());

    // Connecting to a non-routable IP address to trigger connection timeout
    assertThatThrownBy(
            () ->
                client.newCall(new Request.Builder().url("http://10.255.255.1").build()).execute())
        .isInstanceOf(SocketTimeoutException.class)
        .matches(
            (Predicate<Throwable>)
                throwable -> throwable.getMessage().equalsIgnoreCase("connect timed out"));

    verify(isRetryableException, times(5)).apply(any());
    // Should retry maxAttempts, and sleep maxAttempts - 1 times
    verify(sleeper, times(4)).sleep(anyLong());
  }

  @Test
  void nonRetryableException() throws InterruptedException {
    // Override isRetryableException so that no exception is retryable
    when(isRetryableException.apply(any())).thenReturn(false);

    // Connecting to a non-routable IP address to trigger connection timeout
    assertThatThrownBy(
            () ->
                client.newCall(new Request.Builder().url("http://10.255.255.1").build()).execute())
        .isInstanceOf(SocketTimeoutException.class)
        .matches(
            (Predicate<Throwable>)
                throwable -> throwable.getMessage().equalsIgnoreCase("connect timed out"));

    verify(isRetryableException, times(1)).apply(any());
    verify(sleeper, never()).sleep(anyLong());
  }

  @Test
  void isRetryableException() {
    assertThat(RetryInterceptor.isRetryableException(new SocketTimeoutException("error"))).isTrue();
    assertThat(RetryInterceptor.isRetryableException(new IOException("error"))).isFalse();
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
