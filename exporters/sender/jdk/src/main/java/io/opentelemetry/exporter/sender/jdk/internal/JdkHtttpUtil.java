/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.jdk.internal;

import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Utilities for JDK HTTP.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class JdkHtttpUtil {
  @SuppressWarnings("NonFinalStaticField")
  private static boolean propagateContextForTestingInDispatcher = false;

  public static void setPropagateContextForTestingInDispatcher(
      boolean propagateContextForTestingInDispatcher) {
    JdkHtttpUtil.propagateContextForTestingInDispatcher = propagateContextForTestingInDispatcher;
  }

  /** Returns an {@link ExecutorService} using daemon threads. */
  public static ExecutorService newExecutor() {
    return new ThreadPoolExecutor(
        0,
        Integer.MAX_VALUE,
        60,
        TimeUnit.SECONDS,
        new SynchronousQueue<>(),
        new DaemonThreadFactory("jdkhttp-executor", propagateContextForTestingInDispatcher));
  }

  private JdkHtttpUtil() {}
}
