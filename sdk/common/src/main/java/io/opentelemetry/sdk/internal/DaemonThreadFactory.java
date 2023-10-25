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
  private final boolean propagateContext;

  public DaemonThreadFactory(String namePrefix) {
    this(namePrefix, /* propagateContext= */ false);
  }

  public DaemonThreadFactory(String namePrefix, boolean propagateContext) {
    this.namePrefix = namePrefix;
    this.propagateContext = propagateContext;
  }

  @Override
  public Thread newThread(Runnable runnable) {
    Thread t = delegate.newThread(propagateContext ? Context.current().wrap(runnable) : runnable);
    try {
      t.setDaemon(true);
      t.setName(namePrefix + "-" + counter.incrementAndGet());
    } catch (SecurityException e) {
      // Well, we tried.
    }
    return t;
  }
}
