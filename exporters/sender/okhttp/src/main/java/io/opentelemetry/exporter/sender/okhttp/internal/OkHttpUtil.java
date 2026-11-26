/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.internal.DaemonThreadFactory;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import okhttp3.Credentials;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;

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

  /**
   * Returns a {@link Dispatcher} using daemon threads, with both the maximum number of concurrent
   * requests and the maximum size of the backing thread pool bounded to {@code
   * max(availableProcessors, 5)}, matching the bound used by {@code JdkHttpSender}.
   */
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

  /**
   * Applies {@code proxyOptions} to the {@code builder}. If {@code proxyOptions} is {@code null},
   * the builder is left unchanged and OkHttp's default proxy behavior applies.
   *
   * <p>If {@link ProxyOptions#getUsername()} is set, a proxy authenticator is installed which
   * responds to proxy challenges, including OkHttp's preemptive challenge for {@code CONNECT}
   * tunnels, with Basic credentials. The credentials are only sent to the proxy, never to the
   * target server.
   */
  static void configureProxy(OkHttpClient.Builder builder, @Nullable ProxyOptions proxyOptions) {
    if (proxyOptions == null) {
      return;
    }

    builder.proxySelector(proxyOptions.getProxySelector());
    String username = proxyOptions.getUsername();
    String password = proxyOptions.getPassword();

    if (username == null || password == null) {
      return;
    }
    String credential = Credentials.basic(username, password, StandardCharsets.UTF_8);
    builder.proxyAuthenticator((route, response) -> {
      if (response.request().header("Proxy-Authorization") != null) {
        // Credentials were already sent and rejected; give up rather than retry forever.
        return null;
      }
      return response.request().newBuilder().header("Proxy-Authorization", credential).build();
    });
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
