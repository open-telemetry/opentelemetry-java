/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.OptionalLong;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;
import okhttp3.Call;

/** Shared retry policy state and mechanics for OkHttp senders. */
final class RetryState {

  private final RetryPolicy retryPolicy;
  private final Predicate<IOException> retryExceptionPredicate;
  private final Sleeper sleeper;
  private final Supplier<Double> randomJitter;
  private final long timeoutNanos;
  private final LongSupplier nanoTime;
  private final long startTimeNanos;
  private long nextBackoffNanos;

  RetryState(RetryPolicy retryPolicy) {
    this(
        retryPolicy,
        retryPolicy.getRetryExceptionPredicate() == null
            ? RetryState::isRetryableException
            : retryPolicy.getRetryExceptionPredicate(),
        TimeUnit.NANOSECONDS::sleep,
        () -> ThreadLocalRandom.current().nextDouble(0.8d, 1.2d),
        0);
  }

  RetryState(RetryPolicy retryPolicy, long timeoutNanos) {
    this(
        retryPolicy,
        retryPolicy.getRetryExceptionPredicate() == null
            ? RetryState::isRetryableException
            : retryPolicy.getRetryExceptionPredicate(),
        TimeUnit.NANOSECONDS::sleep,
        () -> ThreadLocalRandom.current().nextDouble(0.8d, 1.2d),
        timeoutNanos);
  }

  // Visible for testing.
  RetryState(
      RetryPolicy retryPolicy,
      Predicate<IOException> retryExceptionPredicate,
      Sleeper sleeper,
      Supplier<Double> randomJitter) {
    this(retryPolicy, retryExceptionPredicate, sleeper, randomJitter, 0);
  }

  // Visible for testing.
  RetryState(
      RetryPolicy retryPolicy,
      Predicate<IOException> retryExceptionPredicate,
      Sleeper sleeper,
      Supplier<Double> randomJitter,
      long timeoutNanos) {
    this(
        retryPolicy,
        retryExceptionPredicate,
        sleeper,
        randomJitter,
        timeoutNanos,
        System::nanoTime);
  }

  // Visible for testing.
  RetryState(
      RetryPolicy retryPolicy,
      Predicate<IOException> retryExceptionPredicate,
      Sleeper sleeper,
      Supplier<Double> randomJitter,
      long timeoutNanos,
      LongSupplier nanoTime) {
    this.retryPolicy = retryPolicy;
    this.retryExceptionPredicate = retryExceptionPredicate;
    this.sleeper = sleeper;
    this.randomJitter = randomJitter;
    this.timeoutNanos = timeoutNanos;
    this.nanoTime = nanoTime;
    this.startTimeNanos = nanoTime.getAsLong();
    this.nextBackoffNanos = retryPolicy.getInitialBackoff().toNanos();
  }

  static void defaultSleeper(long delayNanos) throws InterruptedException {
    TimeUnit.NANOSECONDS.sleep(delayNanos);
  }

  static double defaultRandomJitter() {
    return ThreadLocalRandom.current().nextDouble(0.8d, 1.2d);
  }

  boolean backoff(OptionalLong retryDelayNanos) {
    long remainingNanos = remainingNanos();
    if (remainingNanos <= 0) {
      return false;
    }
    long currentBackoffNanos = Math.min(nextBackoffNanos, retryPolicy.getMaxBackoff().toNanos());
    long requestedBackoffNanos =
        retryDelayNanos.isPresent()
            ? retryDelayNanos.getAsLong()
            : (long) (randomJitter.get() * currentBackoffNanos);
    long backoffNanos = Math.min(requestedBackoffNanos, remainingNanos);
    nextBackoffNanos = (long) (currentBackoffNanos * retryPolicy.getBackoffMultiplier());
    try {
      sleeper.sleep(backoffNanos);
      return remainingNanos() > 0;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return false;
    }
  }

  long remainingNanos() {
    if (timeoutNanos <= 0) {
      return Long.MAX_VALUE;
    }
    return Math.max(0, timeoutNanos - (nanoTime.getAsLong() - startTimeNanos));
  }

  boolean configureCallTimeout(Call call) {
    long remainingNanos = remainingNanos();
    if (remainingNanos <= 0) {
      return false;
    }
    if (timeoutNanos > 0) {
      call.timeout().timeout(remainingNanos, TimeUnit.NANOSECONDS);
    }
    return true;
  }

  boolean canRetry(int attempt) {
    return attempt + 1 < retryPolicy.getMaxAttempts();
  }

  boolean shouldRetryOnException(IOException exception) {
    return retryExceptionPredicate.test(exception);
  }

  // Visible for testing.
  static boolean isRetryableException(IOException e) {
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

  @FunctionalInterface
  interface Sleeper {
    void sleep(long delayNanos) throws InterruptedException;
  }
}
