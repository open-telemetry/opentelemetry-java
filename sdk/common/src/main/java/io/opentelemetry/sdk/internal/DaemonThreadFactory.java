/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.context.Context;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

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
  private final ThreadFactory delegate = Executors.defaultThreadFactory();
  private final boolean propagateContextForTesting;

  public DaemonThreadFactory(String namePrefix) {
    this(namePrefix, /* propagateContextForTesting= */ false);
  }

  /**
   * {@link DaemonThreadFactory}'s constructor.
   *
   * @param namePrefix Used when setting the new thread's name.
   * @param propagateContextForTesting For tests only. When enabled, the current thread's {@link
   *     Context} will be passed over to the new threads, this is useful for validating scenarios
   *     where context propagation is available through bytecode instrumentation.
   */
  public DaemonThreadFactory(String namePrefix, boolean propagateContextForTesting) {
    this.namePrefix = namePrefix;
    this.propagateContextForTesting = propagateContextForTesting;
  }

  @Override
  public Thread newThread(Runnable runnable) {
    Thread t =
        delegate.newThread(
            propagateContextForTesting ? Context.current().wrap(runnable) : runnable);
    try {
      t.setDaemon(true);
      t.setName(namePrefix + "-" + counter.incrementAndGet());
      t.setContextClassLoader(null);
    } catch (SecurityException e) {
      // Well, we tried.
    }
    return t;
  }
}
