/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.time.Duration;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.jupiter.api.Test;

class RetryStateTest {

  @Test
  void backoffIsCappedByRemainingTimeout() {
    AtomicLong clock = new AtomicLong();
    AtomicLong sleptNanos = new AtomicLong();
    RetryState retryState =
        new RetryState(
            retryPolicy(),
            (IOException e) -> true,
            delay -> {
              sleptNanos.set(delay);
              clock.addAndGet(delay);
            },
            () -> 1.0d,
            TimeUnit.MILLISECONDS.toNanos(100),
            clock::get);
    clock.set(TimeUnit.MILLISECONDS.toNanos(40));

    assertThat(retryState.backoff(OptionalLong.empty())).isFalse();
    assertThat(sleptNanos.get()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(60));
    assertThat(retryState.remainingNanos()).isZero();
  }

  @Test
  void backoffDoesNotSleepAfterTimeoutExpires() {
    AtomicLong clock = new AtomicLong();
    AtomicLong sleepCalls = new AtomicLong();
    RetryState retryState =
        new RetryState(
            retryPolicy(),
            (IOException e) -> true,
            delay -> sleepCalls.incrementAndGet(),
            () -> 1.0d,
            TimeUnit.MILLISECONDS.toNanos(100),
            clock::get);
    clock.set(TimeUnit.MILLISECONDS.toNanos(100));

    assertThat(retryState.backoff(OptionalLong.empty())).isFalse();
    assertThat(sleepCalls).hasValue(0);
  }

  @Test
  void callTimeoutUsesRemainingExportBudget() {
    AtomicLong clock = new AtomicLong();
    RetryState retryState =
        new RetryState(
            retryPolicy(),
            (IOException e) -> true,
            delay -> {},
            () -> 1.0d,
            TimeUnit.SECONDS.toNanos(1),
            clock::get);
    clock.set(TimeUnit.MILLISECONDS.toNanos(400));
    Call call = new OkHttpClient().newCall(new Request.Builder().url("http://localhost/").build());

    assertThat(retryState.configureCallTimeout(call)).isTrue();
    assertThat(call.timeout().timeoutNanos()).isEqualTo(TimeUnit.MILLISECONDS.toNanos(600));
  }

  @Test
  void callTimeoutIsUnchangedWithoutExportTimeout() {
    RetryState retryState =
        new RetryState(retryPolicy(), (IOException e) -> true, delay -> {}, () -> 1.0d, 0);
    Call call = new OkHttpClient().newCall(new Request.Builder().url("http://localhost/").build());

    assertThat(retryState.configureCallTimeout(call)).isTrue();
    assertThat(call.timeout().timeoutNanos()).isZero();
  }

  @Test
  void callTimeoutIsRejectedAfterExportBudgetExpires() {
    AtomicLong clock = new AtomicLong();
    RetryState retryState =
        new RetryState(
            retryPolicy(),
            (IOException e) -> true,
            delay -> {},
            () -> 1.0d,
            TimeUnit.SECONDS.toNanos(1),
            clock::get);
    clock.set(TimeUnit.SECONDS.toNanos(1));
    Call call = new OkHttpClient().newCall(new Request.Builder().url("http://localhost/").build());

    assertThat(retryState.configureCallTimeout(call)).isFalse();
  }

  @Test
  void retryPredicateAndAttemptLimitAreRespected() {
    RetryState retryState =
        new RetryState(
            retryPolicy(),
            exception -> exception.getMessage().startsWith("retry"),
            delay -> {},
            () -> 1.0d);

    assertThat(retryState.canRetry(0)).isTrue();
    assertThat(retryState.canRetry(1)).isTrue();
    assertThat(retryState.canRetry(2)).isFalse();
    assertThat(retryState.shouldRetryOnException(new IOException("retry this"))).isTrue();
    assertThat(retryState.shouldRetryOnException(new IOException("stop"))).isFalse();
  }

  private static RetryPolicy retryPolicy() {
    return RetryPolicy.builder()
        .setInitialBackoff(Duration.ofMillis(300))
        .setMaxBackoff(Duration.ofMillis(300))
        .setMaxAttempts(3)
        .build();
  }
}
