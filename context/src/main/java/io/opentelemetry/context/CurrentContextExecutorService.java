/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

class CurrentContextExecutorService extends ForwardingExecutorService {

  CurrentContextExecutorService(ExecutorService delegate) {
    super(delegate);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    Objects.requireNonNull(task, "task");
    return delegate().submit(Context.current().wrap(task));
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    Objects.requireNonNull(task, "task");
    return delegate().submit(Context.current().wrap(task), result);
  }

  @Override
  public Future<?> submit(Runnable task) {
    Objects.requireNonNull(task, "task");
    return delegate().submit(Context.current().wrap(task));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    Objects.requireNonNull(tasks, "tasks");
    return delegate().invokeAll(wrap(Context.current(), tasks));
  }

  @Override
  public <T> List<Future<T>> invokeAll(
      Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException {
    Objects.requireNonNull(tasks, "tasks");
    return delegate().invokeAll(wrap(Context.current(), tasks), timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    Objects.requireNonNull(tasks, "tasks");
    return delegate().invokeAny(wrap(Context.current(), tasks));
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    Objects.requireNonNull(tasks, "tasks");
    return delegate().invokeAny(wrap(Context.current(), tasks), timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    Objects.requireNonNull(command, "command");
    delegate().execute(Context.current().wrap(command));
  }
}
