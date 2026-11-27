/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * A {@link ThreadFactory} that delegates to {@code Executors.defaultThreadFactory()} and marks all
 * threads as daemon.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @deprecated Use {@link io.opentelemetry.sdk.common.impl.DaemonThreadFactory} instead.
 */
@Deprecated
public final class DaemonThreadFactory implements ThreadFactory {
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
    this.delegate =
        io.opentelemetry.sdk.common.impl.DaemonThreadFactory.createWithDelegate(
            namePrefix, delegate);
  }

  @Override
  public Thread newThread(Runnable runnable) {
    return delegate.newThread(runnable);
  }
}
