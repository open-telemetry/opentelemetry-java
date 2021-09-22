/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.retry;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningScheduledExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.SettableFuture;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class RetryExecutor {

  private final ListeningScheduledExecutorService executor;
  private final RetryPolicy retryPolicy;
  private final Predicate<Throwable> shouldAbort;

  /** Construct a retry executor. */
  public RetryExecutor(
      String executorThreadNamePrefix, RetryPolicy retryPolicy, Predicate<Throwable> shouldAbort) {
    ScheduledExecutorService executor =
        Executors.newSingleThreadScheduledExecutor(
            new ThreadFactoryBuilder()
                .setNameFormat(executorThreadNamePrefix)
                .setDaemon(true)
                .build());
    this.executor = MoreExecutors.listeningDecorator(executor);
    this.retryPolicy = retryPolicy;
    this.shouldAbort = shouldAbort;
  }

  /** Execute the supplier asynchronously until successful or the retry policy is exceeded. */
  public <T> ListenableFuture<T> submit(
      Function<RetryContext, ListenableFuture<T>> futureSupplier) {
    SettableFuture<T> overallResult = SettableFuture.create();
    submitInternal(new RetryContext(), overallResult, futureSupplier);
    return overallResult;
  }

  @SuppressWarnings({"CheckReturnValue", "FutureReturnValueIgnored"})
  private <T> void submitInternal(
      RetryContext retryContext,
      SettableFuture<T> overallResult,
      Function<RetryContext, ListenableFuture<T>> futureSupplier) {
    int attemptCount = retryContext.getAttemptCount();
    if (attemptCount > 0 && !retryPolicy.shouldRetry(retryContext)) {
      Throwable error = retryContext.getLastAttemptFailure();
      if (error == null) {
        error = new RuntimeException("No previous failure but not retrying.");
      }
      overallResult.setException(error);
      return;
    }
    long delayMillis = attemptCount == 0 ? 0 : retryPolicy.retryDelayMillis(retryContext);
    executor.schedule(
        () -> {
          retryContext.startAttempt();
          Futures.addCallback(
              futureSupplier.apply(retryContext),
              new FutureCallback<T>() {
                @Override
                public void onSuccess(@Nullable T result) {
                  retryContext.complete();
                  overallResult.set(result);
                }

                @Override
                public void onFailure(Throwable t) {
                  retryContext.complete(t);
                  if (shouldAbort.test(t)) {
                    overallResult.setException(t);
                    return;
                  }
                  submitInternal(retryContext, overallResult, futureSupplier);
                }
              },
              executor);
        },
        delayMillis,
        TimeUnit.MILLISECONDS);
  }
}
