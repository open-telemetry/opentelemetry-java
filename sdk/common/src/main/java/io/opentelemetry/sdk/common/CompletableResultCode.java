/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This class models JDK 8's CompletableFuture to afford migration should Open Telemetry's SDK
 * select JDK 8 or greater as a baseline, and also to offer familiarity to developers.
 *
 * <p>The implementation of Export operations are often asynchronous in nature, hence the need to
 * convey a result at a later time. CompletableResultCode facilitates this.
 */
public final class CompletableResultCode {
  /** Returns a {@link CompletableResultCode} that has been completed successfully. */
  public static CompletableResultCode ofSuccess() {
    return SUCCESS;
  }

  /** Returns a {@link CompletableResultCode} that has been completed unsuccessfully. */
  public static CompletableResultCode ofFailure() {
    return FAILURE;
  }

  /**
   * Returns a {@link CompletableResultCode} that completes after all the provided {@link
   * CompletableResultCode}s complete. If any of the results fail, the result will be failed.
   */
  @SuppressWarnings("rawtypes")
  public static CompletableResultCode ofAll(final Collection<CompletableResultCode> codes) {
    ArrayList<CompletableFuture<Boolean>> futures = new ArrayList<>(codes.size());
    codes.forEach(completableResultCode -> futures.add(completableResultCode.future));
    CompletableFuture<Boolean> allFuturesResult =
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .thenApply(
                ignored ->
                    codes.stream()
                        .map(code -> code.future.join())
                        .reduce(Boolean::logicalAnd)
                        .orElse(Boolean.TRUE));
    return new CompletableResultCode(allFuturesResult);
  }

  private static final CompletableResultCode SUCCESS = new CompletableResultCode().succeed();
  private static final CompletableResultCode FAILURE = new CompletableResultCode().fail();

  public CompletableResultCode() {
    this(new CompletableFuture<>());
  }

  private CompletableResultCode(CompletableFuture<Boolean> future) {
    this.future = future;
  }

  private final CompletableFuture<Boolean> future;

  /** Complete this {@link CompletableResultCode} successfully if it is not already completed. */
  public CompletableResultCode succeed() {
    future.complete(Boolean.TRUE);
    return this;
  }

  /** Complete this {@link CompletableResultCode} unsuccessfully if it is not already completed. */
  public CompletableResultCode fail() {
    future.complete(Boolean.FALSE);
    return this;
  }

  /**
   * Obtain the current state of completion. Generally call once completion is achieved via the
   * thenRun method.
   *
   * @return the current state of completion
   */
  public boolean isSuccess() {
    return future.getNow(Boolean.FALSE);
  }

  /**
   * Perform an action on completion. Actions are guaranteed to be called only once.
   *
   * @param action the action to perform
   * @return this completable result so that it may be further composed
   */
  public CompletableResultCode whenComplete(Runnable action) {
    return new CompletableResultCode(future.whenComplete((value, throwable) -> action.run()));
  }

  /** Returns whether this {@link CompletableResultCode} has completed. */
  public boolean isDone() {
    return future.isDone();
  }

  /**
   * Waits for the specified amount of time for this {@link CompletableResultCode} to complete. If
   * it times out or is interrupted, the {@link CompletableResultCode} is failed.
   *
   * @return this {@link CompletableResultCode}
   */
  public CompletableResultCode join(long timeout, TimeUnit unit) {
    try {
      future.get(timeout, unit);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      fail();
    } catch (ExecutionException | TimeoutException e) {
      fail();
    }
    return this;
  }
}
