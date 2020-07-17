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

package io.opentelemetry.sdk.common.export;

import java.util.ArrayList;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

/**
 * The implementation of Export operations are often asynchronous in nature, hence the need to
 * convey a result at a later time. CompletableResultCode facilitates this.
 *
 * <p>This class models JDK 8's CompletableFuture to afford migration should Open Telemetry's SDK
 * select JDK 8 or greater as a baseline, and also to offer familiarity to developers.
 */
public class CompletableResultCode {
  /** A convenience for declaring success. */
  public static CompletableResultCode ofSuccess() {
    return SUCCESS;
  }

  /** A convenience for declaring failure. */
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
  private final ArrayList<Runnable> completionActions = new ArrayList<>();

  private final Object lock = new Object();

  /** The export operation finished successfully. */
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

  /** The export operation finished with an error. */
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
}
