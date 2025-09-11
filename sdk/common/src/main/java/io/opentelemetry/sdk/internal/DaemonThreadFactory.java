/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

/**
 * A {@link ThreadFactory} that delegates to {@code Executors.defaultThreadFactory()} and marks all
 * threads as daemon.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DaemonThreadFactory implements ThreadFactory {
  private final String namePrefix;
  private final AtomicInteger counter = new AtomicInteger();
  private final ThreadFactory delegate;

  public DaemonThreadFactory(String namePrefix) {
    this(namePrefix, Executors.defaultThreadFactory());
  }

  /**
   * {@link DaemonThreadFactory}'s constructor.
   *
   * @param namePrefix Used when setting the new thread's name.
   * @param delegate Delegate to create threads.
   */
  public DaemonThreadFactory(String namePrefix, ThreadFactory delegate) {
    this.namePrefix = namePrefix;
    this.delegate = delegate;
  }

  @Override
  public Thread newThread(Runnable runnable) {
    Thread t = delegate.newThread(runnable);
    t.setUncaughtExceptionHandler(
        new ManagedUncaughtExceptionHandler(t.getUncaughtExceptionHandler()));
    try {
      t.setDaemon(true);
      t.setName(namePrefix + "-" + counter.incrementAndGet());
    } catch (SecurityException e) {
      // Well, we tried.
    }
    return t;
  }

  static class ManagedUncaughtExceptionHandler implements Thread.UncaughtExceptionHandler {
    @Nullable private final Thread.UncaughtExceptionHandler delegate;

    private ManagedUncaughtExceptionHandler(@Nullable Thread.UncaughtExceptionHandler delegate) {
      this.delegate = delegate;
    }

    @SuppressWarnings("Interruption")
    @Override
    public void uncaughtException(Thread t, Throwable e) {
      if (e instanceof InterruptedException) {
        t.interrupt();
      } else if (delegate != null) {
        delegate.uncaughtException(t, e);
      }
    }
  }
}
