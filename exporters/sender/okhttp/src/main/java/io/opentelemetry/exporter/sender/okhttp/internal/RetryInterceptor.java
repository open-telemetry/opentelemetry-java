/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.StringJoiner;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
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
  private final Predicate<IOException> retryExceptionPredicate;
  private final Sleeper sleeper;
  private final Supplier<Double> randomJitter;

  /** Constructs a new retrier. */
  public RetryInterceptor(RetryPolicy retryPolicy, Function<Response, Boolean> isRetryable) {
    this(
        retryPolicy,
        isRetryable,
        retryPolicy.getRetryExceptionPredicate() == null
            ? RetryInterceptor::isRetryableException
            : retryPolicy.getRetryExceptionPredicate(),
        TimeUnit.NANOSECONDS::sleep,
        () -> ThreadLocalRandom.current().nextDouble(0.8d, 1.2d));
  }

  // Visible for testing
  RetryInterceptor(
      RetryPolicy retryPolicy,
      Function<Response, Boolean> isRetryable,
      Predicate<IOException> retryExceptionPredicate,
      Sleeper sleeper,
      Supplier<Double> randomJitter) {
    this.retryPolicy = retryPolicy;
    this.isRetryable = isRetryable;
    this.retryExceptionPredicate = retryExceptionPredicate;
    this.sleeper = sleeper;
    this.randomJitter = randomJitter;
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
        long currentBackoffNanos =
            Math.min(nextBackoffNanos, retryPolicy.getMaxBackoff().toNanos());
        long backoffNanos = (long) (randomJitter.get() * currentBackoffNanos);
        nextBackoffNanos = (long) (currentBackoffNanos * retryPolicy.getBackoffMultiplier());
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
        exception = null;
      }
      try {
        response = chain.proceed(chain.request());
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
        } else {
          throw new NullPointerException("response cannot be null.");
        }
      } catch (IOException e) {
        exception = e;
        response = null;
        boolean retryable = retryExceptionPredicate.test(exception);
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
    } while (++attempt < retryPolicy.getMaxAttempts());

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
  boolean shouldRetryOnException(IOException e) {
    return retryExceptionPredicate.test(e);
  }

  // Visible for testing
  static boolean isRetryableException(IOException e) {
    // Known retryable SocketTimeoutException messages: null, "connect timed out", "timeout"
    // Known retryable ConnectTimeout messages: "Failed to connect to
    // localhost/[0:0:0:0:0:0:0:1]:62611"
    // Known retryable UnknownHostException messages: "xxxxxx.com"
    // Known retryable SocketException: Socket closed
    if (e instanceof SocketTimeoutException) {
      return true;
    } else if (e instanceof ConnectException) {
      return true;
    } else if (e instanceof UnknownHostException) {
      return true;
    } else if (e instanceof SocketException) {
      return true;
    }
    return false;
  }

  // Visible for testing
  interface Sleeper {
    void sleep(long delayNanos) throws InterruptedException;
  }
}
