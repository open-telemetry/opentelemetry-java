/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.entities;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * An executor service that runs all jobs immediately on the current thread.
 *
 * <p>We use this so SDK users can determine how to isolate {@link EntityListener}s with the default
 * being no isolation of events.
 */
final class CurrentThreadExecutorService implements ExecutorService {
  private volatile boolean shutdown = false;

  @Override
  public void execute(Runnable command) {
    if (shutdown) {
      throw new RejectedExecutionException("ExecutorService is shut down");
    }
    command.run();
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) {
    if (shutdown) {
      throw new RejectedExecutionException("ExecutorService is shut down");
    }
    // Execute all tasks synchronously and collect their Futures
    return tasks.stream().map(task -> submit(task)).collect(Collectors.toList());
  }

  @Override
  public <T> List<Future<T>> invokeAll(
      Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) {
    return invokeAll(tasks);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    if (shutdown) {
      throw new RejectedExecutionException("ExecutorService is shut down");
    }
    // Execute all tasks synchronously and return first success.
    for (Callable<T> task : tasks) {
      try {
        // We wrap the task in a `submit` call to get ExecutionExceptions.
        return submit(task).get();
      } catch (ExecutionException e) {
        // Ignore this error, and try the next one.
      }
    }
    throw new ExecutionException("No tasks completed successfully", null);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException {
    if (shutdown) {
      throw new RejectedExecutionException("ExecutorService is shut down");
    }
    return invokeAny(tasks);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    if (shutdown) {
      throw new RejectedExecutionException("ExecutorService is shut down");
    }
    FutureTask<T> future = new FutureTask<>(task);
    // Run in this thread.
    future.run();
    return future;
  }

  @Override
  public Future<?> submit(Runnable task) {
    if (shutdown) {
      throw new RejectedExecutionException("ExecutorService is shut down");
    }
    FutureTask<?> future = new FutureTask<>(task, null);
    // Run in this thread.
    future.run();
    return future;
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    if (shutdown) {
      throw new RejectedExecutionException("ExecutorService is shut down");
    }
    FutureTask<T> future = new FutureTask<>(task, result);
    // Run in this thread.
    future.run();
    return future;
  }

  @Override
  public boolean isShutdown() {
    return shutdown;
  }

  @Override
  public boolean isTerminated() {
    return shutdown;
  }

  @Override
  public void shutdown() {
    shutdown = true;
  }

  @Override
  public List<Runnable> shutdownNow() {
    shutdown = true;
    return Collections.emptyList();
  }

  @Override
  public boolean awaitTermination(long timeout, TimeUnit unit) {
    return isTerminated();
  }
}
