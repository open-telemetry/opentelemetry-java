/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static io.opentelemetry.api.internal.Utils.checkArgument;

import io.opentelemetry.sdk.common.export.ProxyOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import javax.net.SocketFactory;
import okhttp3.Credentials;
import okhttp3.Dns;

/**
 * A {@link SocketFactory} whose sockets reach the target through an HTTP proxy {@code CONNECT}
 * tunnel.
 *
 * <p>OkHttp 4.x only tunnels through HTTP proxies for TLS connections. Plaintext gRPC uses HTTP/2
 * with prior knowledge, which OkHttp 4.x would send directly to the proxy. To behave consistently
 * across OkHttp versions, the OkHttp client is configured with {@link Proxy#NO_PROXY} and this
 * factory, which establishes the tunnel itself before handing the socket to OkHttp.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
final class GrpcH2cConnectTunnelSocketFactory extends SocketFactory {

  /**
   * A {@link Dns} which does not resolve hostnames locally, since the proxy resolves the target.
   * The returned address retains the hostname, which the tunnel socket uses for the {@code CONNECT}
   * request.
   */
  static final Dns UNRESOLVED_DNS =
      hostname -> {
        try {
          return Collections.singletonList(InetAddress.getByAddress(hostname, new byte[4]));
        } catch (UnknownHostException e) {
          // Only thrown for illegal address lengths, which can't happen here.
          throw new IllegalStateException(e);
        }
      };

  private static final int MAX_RESPONSE_HEADER_BYTES = 8192;

  private final ProxySelector proxySelector;

  private final URI uri;

  @Nullable private final String proxyAuthorization;

  GrpcH2cConnectTunnelSocketFactory(ProxyOptions proxyOptions, URI uri) {
    this.proxySelector = proxyOptions.getProxySelector();
    this.uri = uri;

    String username = proxyOptions.getUsername();
    String password = proxyOptions.getPassword();
    this.proxyAuthorization =
        username != null && password != null
            ? Credentials.basic(username, password, StandardCharsets.UTF_8)
            : null;
  }

  /**
   * Returns {@code true} if {@code proxyOptions} selects an HTTP proxy for {@code uri}, in which
   * case this factory should be used.
   */
  static boolean selectsHttpProxy(ProxyOptions proxyOptions, URI uri) {
    return selectHttpProxy(proxyOptions.getProxySelector(), uri) != null;
  }

  @Nullable
  private static Proxy selectHttpProxy(ProxySelector proxySelector, URI uri) {
    List<Proxy> proxies = proxySelector.select(uri);
    if (proxies == null) {
      return null;
    }
    for (Proxy proxy : proxies) {
      if (proxy.type() == Proxy.Type.HTTP) {
        return proxy;
      }
    }
    return null;
  }

  @Override
  public Socket createSocket() {
    return new TunnelSocket();
  }

  @Override
  public Socket createSocket(String host, int port) throws IOException {
    Socket socket = createSocket();
    socket.connect(InetSocketAddress.createUnresolved(host, port));

    return socket;
  }

  @Override
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
      throws IOException {
    Socket socket = createSocket();
    socket.bind(new InetSocketAddress(localHost, localPort));

    socket.connect(InetSocketAddress.createUnresolved(host, port));
    return socket;
  }

  @Override
  public Socket createSocket(InetAddress host, int port) throws IOException {
    Socket socket = createSocket();
    socket.connect(new InetSocketAddress(host, port));
    return socket;
  }

  @Override
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
      throws IOException {
    Socket socket = createSocket();
    socket.bind(new InetSocketAddress(localAddress, localPort));

    socket.connect(new InetSocketAddress(address, port));
    return socket;
  }

  private final class TunnelSocket extends Socket {

    @Override
    public void connect(SocketAddress endpoint, int timeout) throws IOException {
      checkArgument(endpoint instanceof InetSocketAddress, "Unsupported address type");
      InetSocketAddress target = (InetSocketAddress) endpoint;
      String host = target.getHostString();

      int port = target.getPort();
      Proxy proxy = selectHttpProxy(proxySelector, uri);
      if (proxy == null) {
        // The selector no longer picks an HTTP proxy. Connect directly, resolving the hostname
        // here since UNRESOLVED_DNS skipped resolution.
        super.connect(new InetSocketAddress(host, port), timeout);
        return;
      }

      SocketAddress proxyAddress = proxy.address();
      if (proxyAddress instanceof InetSocketAddress
          && ((InetSocketAddress) proxyAddress).isUnresolved()) {
        InetSocketAddress unresolved = (InetSocketAddress) proxyAddress;
        proxyAddress = new InetSocketAddress(unresolved.getHostString(), unresolved.getPort());
      }

      try {
        super.connect(proxyAddress, timeout);
      } catch (IOException e) {
        proxySelector.connectFailed(uri, proxyAddress, e);
        throw e;
      }

      int originalSoTimeout = getSoTimeout();
      try {

        setSoTimeout(timeout);
        establishTunnel(host, port);
        setSoTimeout(originalSoTimeout);

      } catch (IOException | RuntimeException e) {
        close();
        throw e;
      }
    }

    private void establishTunnel(String host, int port) throws IOException {
      String authority = (host.indexOf(':') >= 0 ? "[" + host + "]" : host) + ":" + port;
      StringBuilder request =
          new StringBuilder()
              .append("CONNECT ")
              .append(authority)
              .append(" HTTP/1.1\r\n")
              .append("Host: ")
              .append(authority)
              .append("\r\n");

      if (proxyAuthorization != null) {
        request.append("Proxy-Authorization: ").append(proxyAuthorization).append("\r\n");
      }

      request.append("\r\n");
      OutputStream out = getOutputStream();
      out.write(request.toString().getBytes(StandardCharsets.ISO_8859_1));

      out.flush();
      String statusLine = readResponseHeaders(getInputStream());
      // Status line: HTTP-version SP status-code SP [reason-phrase]
      String[] parts = statusLine.split(" ", 3);
      int statusCode;
      try {
        statusCode = parts.length >= 2 ? Integer.parseInt(parts[1]) : -1;
      } catch (NumberFormatException e) {
        statusCode = -1;
      }
      if (!parts[0].startsWith("HTTP/") || statusCode < 200 || statusCode >= 300) {
        throw new IOException(
            "Failed to establish proxy tunnel to " + authority + ": " + statusLine);
      }
    }
  }

  /**
   * Reads the proxy's response headers, byte by byte so nothing after the header block is consumed,
   * and returns the status line.
   */
  private static String readResponseHeaders(InputStream in) throws IOException {
    ByteArrayOutputStream headers = new ByteArrayOutputStream();
    int matched = 0; // Progress through the "\r\n\r\n" terminator.
    while (matched < 4) {
      int b = in.read();
      if (b == -1) {
        throw new IOException("Proxy closed connection before completing CONNECT response");
      }
      if (headers.size() >= MAX_RESPONSE_HEADER_BYTES) {
        throw new IOException("Proxy CONNECT response headers too large");
      }
      headers.write(b);
      if ((matched % 2 == 0 && b == '\r') || (matched % 2 == 1 && b == '\n')) {
        matched++;
      } else {
        matched = b == '\r' ? 1 : 0;
      }
    }
    String response = new String(headers.toByteArray(), StandardCharsets.ISO_8859_1);
    int endOfStatusLine = response.indexOf("\r\n");
    return response.substring(0, endOfStatusLine);
  }
}
