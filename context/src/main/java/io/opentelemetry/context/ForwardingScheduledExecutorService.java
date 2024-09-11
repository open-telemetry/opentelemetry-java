/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** A {@link ScheduledExecutorService} that implements methods that don't need {@link Context}. */
abstract class ForwardingScheduledExecutorService implements ScheduledExecutorService {

  private final ScheduledExecutorService delegate;

  protected ForwardingScheduledExecutorService(ScheduledExecutorService delegate) {
    this.delegate = delegate;
  }

  ScheduledExecutorService delegate() {
    return delegate;
  }

  @Override
  public final void shutdown() {
    delegate.shutdown();
  }

  @Override
  public final List<Runnable> shutdownNow() {
    return delegate.shutdownNow();
  }

  @Override
  public final boolean isShutdown() {
    return delegate.isShutdown();
  }

  @Override
  public final boolean isTerminated() {
    return delegate.isTerminated();
  }

  @Override
  public final boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
    return delegate.awaitTermination(timeout, unit);
  }

  protected static <T> Collection<? extends Callable<T>> wrap(
      Context context, Collection<? extends Callable<T>> tasks) {
    List<Callable<T>> wrapped = new ArrayList<>();
    for (Callable<T> task : tasks) {
      wrapped.add(context.wrap(task));
    }
    return wrapped;
  }
}
