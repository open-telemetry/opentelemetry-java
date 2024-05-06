/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Configuration for proxy settings.
 *
 * @since 1.36.0
 */
public final class ProxyOptions {
  private final ProxySelector proxySelector;

  private ProxyOptions(ProxySelector proxySelector) {
    this.proxySelector = proxySelector;
  }

  /** Create proxy options with the {@code proxySelector}. */
  public static ProxyOptions create(ProxySelector proxySelector) {
    return new ProxyOptions(proxySelector);
  }

  /**
   * Create proxy options with a {@link ProxySelector} which always uses an {@link Proxy.Type#HTTP}
   * proxy with the {@code socketAddress}.
   */
  public static ProxyOptions create(InetSocketAddress socketAddress) {
    return new ProxyOptions(new SimpleProxySelector(new Proxy(Proxy.Type.HTTP, socketAddress)));
  }

  /** Return the {@link ProxySelector}. */
  public ProxySelector getProxySelector() {
    return proxySelector;
  }

  @Override
  public String toString() {
    return "ProxyOptions{proxySelector=" + proxySelector + "}";
  }

  private static final class SimpleProxySelector extends ProxySelector {

    private final List<Proxy> proxyList;

    private SimpleProxySelector(Proxy proxy) {
      this.proxyList = Collections.singletonList(proxy);
    }

    @Override
    public List<Proxy> select(URI uri) {
      return proxyList;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException e) {
      // ignore
    }

    @Override
    public String toString() {
      return "SimpleProxySelector{proxy=" + proxyList.get(0).toString() + "}";
    }
  }
}
