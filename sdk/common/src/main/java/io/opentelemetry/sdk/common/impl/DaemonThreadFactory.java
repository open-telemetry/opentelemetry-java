/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nullable;

/**
 * A {@link ThreadFactory} that delegates to {@code Executors.defaultThreadFactory()} and marks all
 * threads as daemon.
 *
 * <p>This class is not intended for use by application developers. Its API is stable and will not
 * be changed or removed in a backwards-incompatible manner.
 */
public final class DaemonThreadFactory implements ThreadFactory {
  private final String namePrefix;
  private final AtomicInteger counter = new AtomicInteger();
  private final ThreadFactory delegate;

  private DaemonThreadFactory(String namePrefix, ThreadFactory delegate) {
    this.namePrefix = namePrefix;
    this.delegate = delegate;
  }

  /**
   * Creates a {@link DaemonThreadFactory} with the specified thread name prefix and default thread
   * factory.
   *
   * @param namePrefix used when setting the new thread's name
   * @return the daemon thread factory
   */
  public static DaemonThreadFactory create(String namePrefix) {
    return new DaemonThreadFactory(namePrefix, Executors.defaultThreadFactory());
  }

  /**
   * Creates a {@link DaemonThreadFactory} with the specified thread name prefix and delegate thread
   * factory.
   *
   * @param namePrefix used when setting the new thread's name
   * @param delegate delegate to create threads
   * @return the daemon thread factory
   */
  public static DaemonThreadFactory createWithDelegate(String namePrefix, ThreadFactory delegate) {
    return new DaemonThreadFactory(namePrefix, delegate);
  }

  @Override
  public Thread newThread(Runnable runnable) {
    Thread t = delegate.newThread(runnable);
    t.setUncaughtExceptionHandler(
        new ManagedUncaughtExceptionHandler(t.getUncaughtExceptionHandler()));
    try {
      t.setDaemon(true);
      t.setName(namePrefix + "-" + counter.incrementAndGet());
      t.setContextClassLoader(null);
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
