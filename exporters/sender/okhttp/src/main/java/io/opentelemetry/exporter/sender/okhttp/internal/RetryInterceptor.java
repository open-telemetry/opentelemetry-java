/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Retrier of OkHttp requests.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class RetryInterceptor implements Interceptor {

  private static final Logger logger = Logger.getLogger(RetryInterceptor.class.getName());

  private final RetryPolicy retryPolicy;
  private final Function<Response, Boolean> isRetryable;
  private final Function<IOException, Boolean> isRetryableException;
  private final Sleeper sleeper;
  private final BoundedLongGenerator randomLong;

  /** Constructs a new retrier. */
  public RetryInterceptor(RetryPolicy retryPolicy, Function<Response, Boolean> isRetryable) {
    this(
        retryPolicy,
        isRetryable,
        RetryInterceptor::isRetryableException,
        TimeUnit.NANOSECONDS::sleep,
        bound -> ThreadLocalRandom.current().nextLong(bound));
  }

  // Visible for testing
  RetryInterceptor(
      RetryPolicy retryPolicy,
      Function<Response, Boolean> isRetryable,
      Function<IOException, Boolean> isRetryableException,
      Sleeper sleeper,
      BoundedLongGenerator randomLong) {
    this.retryPolicy = retryPolicy;
    this.isRetryable = isRetryable;
    this.isRetryableException = isRetryableException;
    this.sleeper = sleeper;
    this.randomLong = randomLong;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Response response = null;
    IOException exception = null;
    int attempt = 0;
    long nextBackoffNanos = retryPolicy.getInitialBackoff().toNanos();
    do {
      if (attempt > 0) {
        // Compute and sleep for backoff
        // https://github.com/grpc/proposal/blob/master/A6-client-retries.md#exponential-backoff
        long upperBoundNanos = Math.min(nextBackoffNanos, retryPolicy.getMaxBackoff().toNanos());
        long backoffNanos = randomLong.get(upperBoundNanos);
        nextBackoffNanos = (long) (nextBackoffNanos * retryPolicy.getBackoffMultiplier());
        try {
          sleeper.sleep(backoffNanos);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break; // Break out and return response or throw
        }
        // Close response from previous attempt
        if (response != null) {
          response.close();
        }
      }

      attempt++;
      try {
        response = chain.proceed(chain.request());
      } catch (IOException e) {
        exception = e;
      }
      if (response != null) {
        boolean retryable = Boolean.TRUE.equals(isRetryable.apply(response));
        if (logger.isLoggable(Level.FINER)) {
          logger.log(
              Level.FINER,
              "Attempt "
                  + attempt
                  + " returned "
                  + (retryable ? "retryable" : "non-retryable")
                  + " response: "
                  + responseStringRepresentation(response));
        }
        if (!retryable) {
          return response;
        }
      }
      if (exception != null) {
        boolean retryable = Boolean.TRUE.equals(isRetryableException.apply(exception));
        if (logger.isLoggable(Level.FINER)) {
          logger.log(
              Level.FINER,
              "Attempt "
                  + attempt
                  + " failed with "
                  + (retryable ? "retryable" : "non-retryable")
                  + " exception",
              exception);
        }
        if (!retryable) {
          throw exception;
        }
      }

    } while (attempt < retryPolicy.getMaxAttempts());

    if (response != null) {
      return response;
    }
    throw exception;
  }

  private static String responseStringRepresentation(Response response) {
    StringJoiner joiner = new StringJoiner(",", "Response{", "}");
    joiner.add("code=" + response.code());
    joiner.add(
        "headers="
            + response.headers().toMultimap().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                .collect(joining(",", "[", "]")));
    return joiner.toString();
  }

  // Visible for testing
  static boolean isRetryableException(IOException e) {
    if (e instanceof SocketTimeoutException) {
      String message = e.getMessage();
      // Connect timeouts can produce SocketTimeoutExceptions with no message, or with "connect
      // timed out"
      return message == null || message.toLowerCase(Locale.ROOT).contains("connect timed out");
    } else if (e instanceof ConnectException) {
      // Exceptions resemble: java.net.ConnectException: Failed to connect to
      // localhost/[0:0:0:0:0:0:0:1]:62611
      return true;
    }
    return false;
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
