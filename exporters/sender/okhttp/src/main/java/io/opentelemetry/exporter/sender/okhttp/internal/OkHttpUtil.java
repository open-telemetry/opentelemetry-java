/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import java.net.URI;
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
   * Appends a path to the provided {@link URI} without removing the existing uri's path.
   *
   * @param target The provided URI to append the path to.
   * @param pathToAppend The path to append.
   */
  public static URI appendPathToUri(URI target, String pathToAppend) {
    return URI.create(
            target.getScheme()
                + "://"
                + target.getAuthority()
                + target.getPath()
                + "/"
                + pathToAppend)
        .normalize();
  }

  private OkHttpUtil() {}
}
