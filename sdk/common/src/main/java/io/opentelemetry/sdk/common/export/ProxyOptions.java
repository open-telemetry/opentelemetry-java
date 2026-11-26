/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/**
 * Configuration for proxy settings.
 *
 * @since 1.36.0
 */
public final class ProxyOptions {
  private static final ProxyOptions NO_PROXY =
      new ProxyOptions(NoProxySelector.INSTANCE, null, null);

  private final ProxySelector proxySelector;

  @Nullable private final String username;

  @Nullable private final String password;

  private ProxyOptions(
      ProxySelector proxySelector, @Nullable String username, @Nullable String password) {
    this.proxySelector = proxySelector;
    this.username = username;
    this.password = password;
  }

  /** Create proxy options with the {@code proxySelector}. */
  public static ProxyOptions create(ProxySelector proxySelector) {
    return new ProxyOptions(proxySelector, null, null);
  }

  /**
   * Create proxy options with a {@link ProxySelector} which always uses an {@link Proxy.Type#HTTP}
   * proxy with the {@code socketAddress}.
   */
  public static ProxyOptions create(InetSocketAddress socketAddress) {
    return new ProxyOptions(simpleProxySelector(socketAddress), null, null);
  }

  /**
   * Create proxy options with a {@link ProxySelector} which always uses an {@link Proxy.Type#HTTP}
   * proxy with the {@code socketAddress}, authenticating to the proxy with <a
   * href="https://datatracker.ietf.org/doc/html/rfc7617">Basic authentication</a> using the {@code
   * username} and {@code password}.
   */
  public static ProxyOptions create(
      InetSocketAddress socketAddress, String username, String password) {
    requireNonNull(username, "username");
    requireNonNull(password, "password");
    return new ProxyOptions(simpleProxySelector(socketAddress), username, password);
  }

  /**
   * Create proxy options which never use a proxy, connecting directly to the target. Unlike not
   * setting proxy options at all, this ignores ambient JVM proxy configuration such as the {@code
   * http.proxyHost} and {@code https.proxyHost} system properties or {@link
   * ProxySelector#getDefault()}.
   */
  public static ProxyOptions noProxy() {
    return NO_PROXY;
  }

  private static ProxySelector simpleProxySelector(InetSocketAddress socketAddress) {
    return new SimpleProxySelector(new Proxy(Proxy.Type.HTTP, socketAddress));
  }

  /** Return the {@link ProxySelector}. */
  public ProxySelector getProxySelector() {
    return proxySelector;
  }

  /**
   * Return the username used for Basic authentication to the proxy, or {@code null} if proxy
   * authentication is not configured. If non-null, {@link #getPassword()} is also non-null.
   */
  @Nullable
  public String getUsername() {
    return username;
  }

  /**
   * Return the password used for Basic authentication to the proxy, or {@code null} if proxy
   * authentication is not configured. If non-null, {@link #getUsername()} is also non-null.
   */
  @Nullable
  public String getPassword() {
    return password;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder("ProxyOptions{proxySelector=").append(proxySelector);
    if (username != null) {
      // Never include the password.
      sb.append(", username=").append(username).append(", password=****");
    }
    return sb.append("}").toString();
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

  private static final class NoProxySelector extends ProxySelector {

    private static final NoProxySelector INSTANCE = new NoProxySelector();
    private static final List<Proxy> PROXY_LIST = Collections.singletonList(Proxy.NO_PROXY);

    @Override
    public List<Proxy> select(URI uri) {
      return PROXY_LIST;
    }

    @Override
    public void connectFailed(URI uri, SocketAddress sa, IOException e) {
      // ignore
    }

    @Override
    public String toString() {
      return "NoProxySelector";
    }
  }
}
