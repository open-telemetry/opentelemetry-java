/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import io.opentelemetry.api.internal.GuardedBy;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * This class models JDK 8's CompletableFuture to afford migration should Open Telemetry's SDK
 * select JDK 8 or greater as a baseline, and also to offer familiarity to developers.
 *
 * <p>The implementation of Export operations are often asynchronous in nature, hence the need to
 * convey a result at a later time. CompletableResultCode facilitates this.
 */
public final class CompletableResultCode {

  private static final Logger logger = Logger.getLogger(CompletableResultCode.class.getName());

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
    return complete(/* success= */ true, /* throwable= */ null);
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
   * @param throwable the {@code Throwable} that caused the failure, or {@code null}
   * @since 1.41.0
   */
  public CompletableResultCode failExceptionally(@Nullable Throwable throwable) {
    return failInternal(throwable);
  }

  private CompletableResultCode failInternal(@Nullable Throwable throwable) {
    return complete(/* success= */ false, throwable);
  }

  private CompletableResultCode complete(boolean success, @Nullable Throwable throwable) {
    List<Runnable> actions = null;
    synchronized (lock) {
      if (succeeded == null) {
        succeeded = success;
        if (!success) {
          this.throwable = throwable;
        }
        if (!completionActions.isEmpty()) {
          actions = new ArrayList<>(completionActions);
          completionActions.clear();
        }
      }
    }
    // Completion actions run without the lock held. Running them under the lock lets an action
    // which completes another result deadlock through lock inversion.
    if (actions != null) {
      runActions(actions);
    }
    return this;
  }

  private static void runActions(List<Runnable> actions) {
    RuntimeException firstException = null;
    for (Runnable action : actions) {
      try {
        action.run();
      } catch (RuntimeException e) {
        // Continue executing the remaining actions, so ofAll is not blocked from completing.
        logger.log(Level.WARNING, "Exception thrown by completion action.", e);
        if (firstException == null) {
          firstException = e;
        }
      }
    }
    if (firstException != null) {
      throw firstException;
    }
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
   *     exception}, {@link #succeed() succeeded}, {@link #failExceptionally(Throwable)} with a null
   *     {@code throwable}, or not complete.
   * @since 1.41.0
   */
  @Nullable
  public Throwable getFailureThrowable() {
    synchronized (lock) {
      return throwable;
    }
  }

  /**
   * Perform an action on completion. Actions are guaranteed to be called only once. Actions are not
   * invoked while internal locks are held. Every action runs even if an earlier one throws a {@link
   * RuntimeException}. Each exception is logged, and the first one is rethrown after all the
   * actions have executed.
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
      runActions(Collections.singletonList(action));
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
