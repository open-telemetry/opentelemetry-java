/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import okhttp3.Dispatcher;
import okhttp3.Request;

/**
 * Utilities for OkHttp.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OkHttpUtil {

  /** Returns a {@link Dispatcher} using daemon threads, otherwise matching the OkHttp default. */
  public static Dispatcher newDispatcher() {
    return new Dispatcher(
        new ThreadPoolExecutor(
            0,
            Integer.MAX_VALUE,
            60,
            TimeUnit.SECONDS,
            new SynchronousQueue<>(),
            new DaemonThreadFactory("okhttp-dispatch")));
  }

  /**
   * Returns a {@link Request.Builder} with a tag to identify a request created by OTel. The tag can
   * later be checked by an automatic instrumentation to avoid tracing OTel exporter's calls.
   */
  public static Request.Builder newRequestBuilder() {
    return new Request.Builder().tag("suppress_instrumentation");
  }

  private OkHttpUtil() {}
}
