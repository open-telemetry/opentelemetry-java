/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import io.opentelemetry.api.internal.GuardedBy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

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
   * Returns a {@link CompletableResultCode} that has been {@link #failExceptionally(Throwable)
   * failed exceptionally}.
   *
   * @since 1.41.0
   */
  public static CompletableResultCode ofExceptionalFailure(Throwable throwable) {
    return new CompletableResultCode().failExceptionally(throwable);
  }

  /**
   * Returns a {@link CompletableResultCode} that completes after all the provided {@link
   * CompletableResultCode}s complete. If any of the results fail, the result will be failed. If any
   * {@link #failExceptionally(Throwable) failed exceptionally}, the result will be failed
   * exceptionally with the first {@link Throwable} from {@code codes}.
   */
  public static CompletableResultCode ofAll(Collection<CompletableResultCode> codes) {
    if (codes.isEmpty()) {
      return ofSuccess();
    }
    CompletableResultCode result = new CompletableResultCode();
    AtomicInteger pending = new AtomicInteger(codes.size());
    AtomicBoolean failed = new AtomicBoolean();
    AtomicReference<Throwable> throwableRef = new AtomicReference<>();
    for (CompletableResultCode code : codes) {
      code.whenComplete(
          () -> {
            if (!code.isSuccess()) {
              failed.set(true);
              Throwable codeThrowable = code.getFailureThrowable();
              if (codeThrowable != null) {
                throwableRef.compareAndSet(null, codeThrowable);
              }
            }
            if (pending.decrementAndGet() == 0) {
              if (failed.get()) {
                result.failInternal(throwableRef.get());
              } else {
                result.succeed();
              }
            }
          });
    }
    return result;
  }

  private static final CompletableResultCode SUCCESS = new CompletableResultCode().succeed();
  private static final CompletableResultCode FAILURE = new CompletableResultCode().fail();

  public CompletableResultCode() {}

  @Nullable
  @GuardedBy("lock")
  private Boolean succeeded = null;

  @Nullable
  @GuardedBy("lock")
  private Throwable throwable = null;

  @GuardedBy("lock")
  private final List<Runnable> completionActions = new ArrayList<>();

  private final Object lock = new Object();

  /** Complete this {@link CompletableResultCode} successfully if it is not already completed. */
  public CompletableResultCode succeed() {
    synchronized (lock) {
      if (succeeded == null) {
        succeeded = true;
        for (Runnable action : completionActions) {
          action.run();
        }
      }
    }
    return this;
  }

  /**
   * Complete this {@link CompletableResultCode} unsuccessfully if it is not already completed,
   * setting the {@link #getFailureThrowable() failure throwable} to {@code null}.
   */
  public CompletableResultCode fail() {
    return failInternal(null);
  }

  /**
   * Completes this {@link CompletableResultCode} unsuccessfully if it is not already completed,
   * setting the {@link #getFailureThrowable() failure throwable} to {@code throwable}.
   *
   * @param throwable the Throwable that caused the failure, or null
   * @since 1.41.0
   */
  public CompletableResultCode failExceptionally(@Nullable Throwable throwable) {
    return failInternal(throwable);
  }

  private CompletableResultCode failInternal(@Nullable Throwable throwable) {
    synchronized (lock) {
      if (succeeded == null) {
        succeeded = false;
        this.throwable = throwable;
        for (Runnable action : completionActions) {
          action.run();
        }
      }
    }
    return this;
  }

  /**
   * Obtain the current state of completion. Generally call once completion is achieved via the
   * {@link #whenComplete(Runnable)} method.
   *
   * @return the current state of completion
   */
  public boolean isSuccess() {
    synchronized (lock) {
      return succeeded != null && succeeded;
    }
  }

  /**
   * Returns {@link Throwable} if this {@link CompletableResultCode} was {@link
   * #failExceptionally(Throwable) failed exceptionally}. Generally call once completion is achieved
   * via the {@link #whenComplete(Runnable)} method.
   *
   * @return the throwable if failed exceptionally, or null if: {@link #fail() failed without
   *     exception}, {@link #succeed() succeeded}, or not complete.g
   * @since 1.41.0
   */
  @Nullable
  public Throwable getFailureThrowable() {
    synchronized (lock) {
      return throwable;
    }
  }

  /**
   * Perform an action on completion. Actions are guaranteed to be called only once.
   *
   * @param action the action to perform
   * @return this completable result so that it may be further composed
   */
  public CompletableResultCode whenComplete(Runnable action) {
    boolean runNow = false;
    synchronized (lock) {
      if (succeeded != null) {
        runNow = true;
      } else {
        this.completionActions.add(action);
      }
    }
    if (runNow) {
      action.run();
    }
    return this;
  }

  /** Returns whether this {@link CompletableResultCode} has completed. */
  public boolean isDone() {
    synchronized (lock) {
      return succeeded != null;
    }
  }

  /**
   * Waits up to the specified amount of time for this {@link CompletableResultCode} to complete.
   * Even after this method returns, the result may not be complete yet - you should always check
   * {@link #isSuccess()} or {@link #isDone()} after calling this method to determine the result.
   *
   * @return this {@link CompletableResultCode}
   */
  public CompletableResultCode join(long timeout, TimeUnit unit) {
    if (isDone()) {
      return this;
    }
    CountDownLatch latch = new CountDownLatch(1);
    whenComplete(latch::countDown);
    try {
      if (!latch.await(timeout, unit)) {
        return this;
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
    return this;
  }
}
