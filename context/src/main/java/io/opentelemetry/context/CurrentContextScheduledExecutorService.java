/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

final class CurrentContextScheduledExecutorService extends ForwardingScheduledExecutorService {

  CurrentContextScheduledExecutorService(ScheduledExecutorService delegate) {
    super(delegate);
  }

  @Override
  public <T> Future<T> submit(Callable<T> task) {
    return delegate().submit(Context.current().wrap(task));
  }

  @Override
  public <T> Future<T> submit(Runnable task, T result) {
    return delegate().submit(Context.current().wrap(task), result);
  }

  @Override
  public Future<?> submit(Runnable task) {
    return delegate().submit(Context.current().wrap(task));
  }

  @Override
  public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
      throws InterruptedException {
    return delegate().invokeAll(wrap(Context.current(), tasks));
  }

  @Override
  public <T> List<Future<T>> invokeAll(
      Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException {
    return delegate().invokeAll(wrap(Context.current(), tasks), timeout, unit);
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks)
      throws InterruptedException, ExecutionException {
    return delegate().invokeAny(wrap(Context.current(), tasks));
  }

  @Override
  public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
      throws InterruptedException, ExecutionException, TimeoutException {
    return delegate().invokeAny(wrap(Context.current(), tasks), timeout, unit);
  }

  @Override
  public void execute(Runnable command) {
    delegate().execute(Context.current().wrap(command));
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return delegate().schedule(Context.current().wrap(command), delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    return delegate().schedule(Context.current().wrap(callable), delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(
      Runnable command, long initialDelay, long period, TimeUnit unit) {
    return delegate()
        .scheduleAtFixedRate(Context.current().wrap(command), initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(
      Runnable command, long initialDelay, long delay, TimeUnit unit) {
    return delegate()
        .scheduleWithFixedDelay(Context.current().wrap(command), initialDelay, delay, unit);
  }
}
