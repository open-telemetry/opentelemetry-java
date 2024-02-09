/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

/** Configuration for proxy settings. */
public class ProxyOptions {
  private final String host;

  private final int port;

  public int getPort() {
    return port;
  }

  public String getHost() {
    return host;
  }

  ProxyOptions(String host, int port) {
    this.host = host;
    this.port = port;
  }

  public static ProxyOptionsBuilder builder(String host, int port) {
    return new ProxyOptions.ProxyOptionsBuilder(host, port);
  }

  public static class ProxyOptionsBuilder {

    private final String host;
    private final int port;

    /** Supply the host name or IP address of the proxy and the port it is working on. */
    ProxyOptionsBuilder(String host, int port) {
      this.host = host;
      this.port = port;
    }

    public ProxyOptions build() {
      ProxyOptions proxyOptions = new ProxyOptions(host, port);

      return proxyOptions;
    }
  }
}
