/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.context;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

class ContextScheduledExecutorService extends ContextExecutorService
    implements ScheduledExecutorService {

  ContextScheduledExecutorService(Context context, ScheduledExecutorService delegate) {
    super(context, delegate);
  }

  @Override
  ScheduledExecutorService delegate() {
    return (ScheduledExecutorService) super.delegate();
  }

  @Override
  public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
    return delegate().schedule(context().wrap(command), delay, unit);
  }

  @Override
  public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
    return delegate().schedule(context().wrap(callable), delay, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleAtFixedRate(
      Runnable command, long initialDelay, long period, TimeUnit unit) {
    return delegate().scheduleAtFixedRate(context().wrap(command), initialDelay, period, unit);
  }

  @Override
  public ScheduledFuture<?> scheduleWithFixedDelay(
      Runnable command, long initialDelay, long delay, TimeUnit unit) {
    return delegate().scheduleWithFixedDelay(context().wrap(command), initialDelay, delay, unit);
  }
}
