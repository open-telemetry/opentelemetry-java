/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.internal.DaemonThreadFactory;
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
  private static final int DEFAULT_MAX_REQUESTS_PER_HOST = 5;

  @SuppressWarnings("NonFinalStaticField")
  private static boolean propagateContextForTestingInDispatcher = false;

  public static void setPropagateContextForTestingInDispatcher(
      boolean propagateContextForTestingInDispatcher) {
    OkHttpUtil.propagateContextForTestingInDispatcher = propagateContextForTestingInDispatcher;
  }

  /** Returns a {@link Dispatcher} using daemon threads, otherwise matching the OkHttp default. */
  public static Dispatcher newDispatcher() {
    int maxRequests = Math.max(Runtime.getRuntime().availableProcessors(), 5);
    Dispatcher dispatcher =
        new Dispatcher(
            new ThreadPoolExecutor(
                0,
                maxRequests,
                60,
                TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                createThreadFactory("okhttp-dispatch")));
    dispatcher.setMaxRequests(maxRequests);
    dispatcher.setMaxRequestsPerHost(DEFAULT_MAX_REQUESTS_PER_HOST);
    return dispatcher;
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
