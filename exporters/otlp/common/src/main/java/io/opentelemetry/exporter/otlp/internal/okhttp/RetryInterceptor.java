/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.okhttp;

import io.opentelemetry.exporter.otlp.internal.RetryPolicy;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import okhttp3.Interceptor;
import okhttp3.Response;

/** Retrier of OkHttp requests. */
public final class RetryInterceptor implements Interceptor {

  private final RetryPolicy retryPolicy;
  private final Function<Response, Boolean> isRetryable;
  private final Sleeper sleeper;
  private final BoundedLongGenerator randomLong;

  /** Constructs a new retrier. */
  public RetryInterceptor(RetryPolicy retryPolicy, Function<Response, Boolean> isRetryable) {
    this(
        retryPolicy,
        isRetryable,
        TimeUnit.NANOSECONDS::sleep,
        bound -> ThreadLocalRandom.current().nextLong(bound));
  }

  // Visible for testing
  RetryInterceptor(
      RetryPolicy retryPolicy,
      Function<Response, Boolean> isRetryable,
      Sleeper sleeper,
      BoundedLongGenerator randomLong) {
    this.retryPolicy = retryPolicy;
    this.isRetryable = isRetryable;
    this.sleeper = sleeper;
    this.randomLong = randomLong;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Response response = chain.proceed(chain.request());
    if (!Boolean.TRUE.equals(isRetryable.apply(response))) {
      return response;
    }
    long nextBackoffNanos = retryPolicy.getInitialBackoff().toNanos();
    // Made an attempt already, starting with the 2nd.
    for (int attempt = 2; attempt <= retryPolicy.getMaxAttempts(); attempt++) {
      // https://github.com/grpc/proposal/blob/master/A6-client-retries.md#exponential-backoff
      // Careful - the document says "retry attempt" but our maxAttempts includes the original
      // request. The numbers are tricky but the unit test should be more clear.
      long upperBoundNanos = Math.min(nextBackoffNanos, retryPolicy.getMaxBackoff().toNanos());
      long backoffNanos = randomLong.get(upperBoundNanos);

      try {
        sleeper.sleep(backoffNanos);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        return response;
      }
      // Close response from previous attempt and try again.
      response.close();
      response = chain.proceed(chain.request());
      if (!Boolean.TRUE.equals(isRetryable.apply(response))) {
        return response;
      }
      nextBackoffNanos = (long) (nextBackoffNanos * retryPolicy.getBackoffMultiplier());
    }
    return response;
  }

  // Visible for testing
  interface BoundedLongGenerator {
    long get(long bound);
  }

  // Visible for testing
  interface Sleeper {
    void sleep(long delayNanos) throws InterruptedException;
  }
}
