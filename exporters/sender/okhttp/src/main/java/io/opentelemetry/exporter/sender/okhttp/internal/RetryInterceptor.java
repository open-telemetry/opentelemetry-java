/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.util.OptionalLong;
import java.util.StringJoiner;
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
  private final Function<Response, OptionalLong> retryDelayNanosExtractor;
  private final Predicate<IOException> retryExceptionPredicate;
  private final Sleeper sleeper;
  private final Supplier<Double> randomJitter;

  /** Constructs a new retrier. */
  public RetryInterceptor(
      RetryPolicy retryPolicy,
      Function<Response, Boolean> isRetryable,
      Function<Response, OptionalLong> retryDelayNanosExtractor) {
    this(
        retryPolicy,
        isRetryable,
        retryDelayNanosExtractor,
        retryPolicy.getRetryExceptionPredicate() == null
            ? RetryState::isRetryableException
            : retryPolicy.getRetryExceptionPredicate(),
        RetryState::defaultSleeper,
        RetryState::defaultRandomJitter);
  }

  // Visible for testing
  RetryInterceptor(
      RetryPolicy retryPolicy,
      Function<Response, Boolean> isRetryable,
      Function<Response, OptionalLong> retryDelayNanosExtractor,
      Predicate<IOException> retryExceptionPredicate,
      Sleeper sleeper,
      Supplier<Double> randomJitter) {
    this.retryPolicy = retryPolicy;
    this.isRetryable = isRetryable;
    this.retryDelayNanosExtractor = retryDelayNanosExtractor;
    this.retryExceptionPredicate = retryExceptionPredicate;
    this.sleeper = sleeper;
    this.randomJitter = randomJitter;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    RetryState retryState =
        new RetryState(
            retryPolicy,
            retryExceptionPredicate,
            sleeper,
            randomJitter,
            chain.call().timeout().timeoutNanos(),
            System::nanoTime);
    Response response = null;
    IOException exception = null;
    int attempt = 0;
    OptionalLong retryDelayNanos = OptionalLong.empty();
    do {
      if (attempt > 0) {
        // Compute and sleep for backoff
        // https://github.com/grpc/proposal/blob/master/A6-client-retries.md#exponential-backoff
        if (!retryState.backoff(retryDelayNanos)) {
          break; // Break out and return response or throw
        }
        retryDelayNanos = OptionalLong.empty();
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
          retryDelayNanos = retryDelayNanosExtractor.apply(response);
        } else {
          throw new NullPointerException("response cannot be null.");
        }
      } catch (IOException e) {
        exception = e;
        response = null;
        boolean retryable = retryState.shouldRetryOnException(exception);
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
  static boolean isRetryableException(IOException e) {
    return RetryState.isRetryableException(e);
  }

  // Visible for testing
  boolean shouldRetryOnException(IOException e) {
    return retryExceptionPredicate.test(e);
  }

  // Visible for testing
  interface Sleeper extends RetryState.Sleeper {}
}
