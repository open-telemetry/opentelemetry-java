/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * This class models JDK 8's CompletableFuture to afford migration should Open Telemetry's SDK
 * select JDK 8 or greater as a baseline, and also to offer familiarity to developers.
 *
 * <p>The implementation of Export operations are often asynchronous in nature, hence the need to
 * convey a result at a later time. CompletableResultCode facilitates this.
 */
public class CompletableResultCode {
  /** Returns a {@link CompletableResultCode} that has been completed successfully. */
  public static CompletableResultCode ofSuccess() {
    return SUCCESS;
  }

  /** Returns a {@link CompletableResultCode} that has been completed unsuccessfully. */
  public static CompletableResultCode ofFailure() {
    return FAILURE;
  }

  private static final CompletableResultCode SUCCESS = new CompletableResultCode().succeed();
  private static final CompletableResultCode FAILURE = new CompletableResultCode().fail();

  public CompletableResultCode() {}

  @Nullable
  @GuardedBy("lock")
  private Boolean succeeded = null;

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

  /** Complete this {@link CompletableResultCode} unsuccessfully if it is not already completed. */
  public CompletableResultCode fail() {
    synchronized (lock) {
      if (succeeded == null) {
        succeeded = false;
        for (Runnable action : completionActions) {
          action.run();
        }
      }
    }
    return this;
  }

  /**
   * Obtain the current state of completion. Generally call once completion is achieved via the
   * thenRun method.
   *
   * @return the current state of completion
   */
  public boolean isSuccess() {
    synchronized (lock) {
      return succeeded != null && succeeded;
    }
  }

  /**
   * Perform an action on completion. Actions are guaranteed to be called only once.
   *
   * @param action the action to perform
   * @return this completable result so that it may be further composed
   */
  public CompletableResultCode whenComplete(Runnable action) {
    synchronized (lock) {
      if (succeeded != null) {
        action.run();
      } else {
        this.completionActions.add(action);
      }
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
   * Waits for the specified amount of time for this {@link CompletableResultCode} to complete. If
   * it times out or is interrupted, the {@link CompletableResultCode} is failed.
   *
   * @return this {@link CompletableResultCode}
   */
  public CompletableResultCode join(long timeout, TimeUnit unit) {
    if (isDone()) {
      return this;
    }
    final CountDownLatch latch = new CountDownLatch(1);
    whenComplete(
        new Runnable() {
          @Override
          public void run() {
            latch.countDown();
          }
        });
    try {
      if (!latch.await(timeout, unit)) {
        fail();
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      fail();
    }
    return this;
  }
}
