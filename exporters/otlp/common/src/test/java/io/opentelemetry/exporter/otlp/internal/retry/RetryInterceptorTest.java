/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.retry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.linecorp.armeria.common.HttpResponse;
import com.linecorp.armeria.common.HttpStatus;
import com.linecorp.armeria.testing.junit5.server.mock.MockWebServerExtension;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
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

  private OkHttpClient client;

  @BeforeEach
  void setUp() {
    RetryInterceptor retrier =
        new RetryInterceptor(
            RetryPolicy.builder()
                .setBackoffMultiplier(1.6)
                .setInitialBackoff(Duration.ofSeconds(1))
                .setMaxBackoff(Duration.ofSeconds(2))
                .setMaxAttempts(5)
                .build(),
            r -> !r.isSuccessful(),
            sleeper,
            random);
    client = new OkHttpClient.Builder().addInterceptor(retrier).build();
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
