/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.Dispatcher;

/**
 * Utilities for OkHttp.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OkHttpUtil {
  @SuppressWarnings("NonFinalStaticField")
  private static boolean propagateContextForTestingInDispatcher = false;

  public static void setPropagateContextForTestingInDispatcher(
      boolean propagateContextForTestingInDispatcher) {
    OkHttpUtil.propagateContextForTestingInDispatcher = propagateContextForTestingInDispatcher;
  }

  /** Returns a {@link Dispatcher} using daemon threads, otherwise matching the OkHttp default. */
  public static Dispatcher newDispatcher() {
    return new Dispatcher(
        new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            createThreadFactory("okhttp-dispatch")));
  }

  private static DaemonThreadFactory createThreadFactory(String namePrefix) {
    if (propagateContextForTestingInDispatcher) {
      return new DaemonThreadFactory(
          namePrefix, r -> Executors.defaultThreadFactory().newThread(Context.current().wrap(r)));
    }
    return new DaemonThreadFactory(namePrefix);
  }

  private OkHttpUtil() {}
}
