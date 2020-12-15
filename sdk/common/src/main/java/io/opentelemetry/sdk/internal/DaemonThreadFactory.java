/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A {@link ThreadFactory} that delegates to {@code Executors.defaultThreadFactory()} and marks all
 * threads as daemon.
 */
public class DaemonThreadFactory implements ThreadFactory {
  private final String namePrefix;
  private final AtomicInteger counter = new AtomicInteger();

  public DaemonThreadFactory(String namePrefix) {
    this.namePrefix = namePrefix;
  }

  @Override
  public Thread newThread(Runnable runnable) {
    Thread t = Executors.defaultThreadFactory().newThread(runnable);
    try {
      t.setDaemon(true);
      t.setName(namePrefix + "-" + counter.incrementAndGet());
    } catch (SecurityException e) {
      // Well, we tried.
    }
    return t;
  }
}
