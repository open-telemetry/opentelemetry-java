/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.common.export.ProxyOptions;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GrpcH2cConnectTunnelSocketFactoryTest {

  private static final URI ENDPOINT = URI.create("http://collector.example:4317");

  private ServerSocket proxyServer;

  @BeforeEach
  void setUp() throws IOException {
    proxyServer = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
  }

  @AfterEach
  void tearDown() throws IOException {
    proxyServer.close();
  }

  private InetSocketAddress proxyAddress() {
    return new InetSocketAddress(InetAddress.getLoopbackAddress(), proxyServer.getLocalPort());
  }

  /** Accepts one connection, captures the request headers, writes {@code response}. */
  private CompletableFuture<String> serveOnce(String response) {
    return CompletableFuture.supplyAsync(() -> {
      try (Socket client = proxyServer.accept()) {
        String request = readHeaders(client.getInputStream());
        try {
          OutputStream out = client.getOutputStream();
          out.write(response.getBytes(StandardCharsets.ISO_8859_1));
          out.flush();
          // Echo one byte so the test can verify the tunnel carries data after the headers.
          int b = client.getInputStream().read();
          if (b != -1) {
            out.write(b);
            out.flush();
          }
        } catch (IOException e) {
          // The client may close the connection early, e.g. on a rejected tunnel.
        }
        return request;
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    });
  }

  private static String readHeaders(InputStream in) throws IOException {
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    while (true) {
      int b = in.read();
      if (b == -1) {
        break;
      }
      buf.write(b);
      byte[] bytes = buf.toByteArray();
      int n = bytes.length;
      if (n >= 4
          && Arrays.equals(
              Arrays.copyOfRange(bytes, n - 4, n), "\r\n\r\n".getBytes(StandardCharsets.UTF_8))) {
        break;
      }
    }
    return new String(buf.toByteArray(), StandardCharsets.ISO_8859_1);
  }

  private static InetSocketAddress target() throws IOException {
    InetAddress address =
        GrpcH2cConnectTunnelSocketFactory.UNRESOLVED_DNS.lookup("collector.example").get(0);
    return new InetSocketAddress(address, 4317);
  }

  @Test
  void tunnelEstablished() throws Exception {
    CompletableFuture<String> request =
        serveOnce("HTTP/1.1 200 Connection established\r\nVia: test\r\n\r\n");
    GrpcH2cConnectTunnelSocketFactory factory =
        new GrpcH2cConnectTunnelSocketFactory(ProxyOptions.create(proxyAddress()), ENDPOINT);

    try (Socket socket = factory.createSocket()) {
      socket.connect(target(), 5000);
      socket.getOutputStream().write('x');
      assertThat(socket.getInputStream().read()).isEqualTo('x');
    }

    assertThat(request.get(5, TimeUnit.SECONDS))
        .startsWith("CONNECT collector.example:4317 HTTP/1.1\r\n")
        .contains("Host: collector.example:4317\r\n")
        .doesNotContain("Proxy-Authorization");
  }

  @Test
  void tunnelEstablished_basicAuth() throws Exception {
    CompletableFuture<String> request = serveOnce("HTTP/1.1 200 OK\r\n\r\n");
    GrpcH2cConnectTunnelSocketFactory factory =
        new GrpcH2cConnectTunnelSocketFactory(
            ProxyOptions.create(proxyAddress(), "user", "pass:word"), ENDPOINT);

    try (Socket socket = factory.createSocket()) {
      socket.connect(target(), 5000);
    }

    assertThat(request.get(5, TimeUnit.SECONDS))
        .contains("Proxy-Authorization: Basic dXNlcjpwYXNzOndvcmQ=\r\n");
  }

  @Test
  void proxyRejects() throws Exception {
    CompletableFuture<String> request =
        serveOnce("HTTP/1.1 407 Proxy Authentication Required\r\n\r\n");
    GrpcH2cConnectTunnelSocketFactory factory =
        new GrpcH2cConnectTunnelSocketFactory(ProxyOptions.create(proxyAddress()), ENDPOINT);

    Socket socket = factory.createSocket();
    assertThatThrownBy(() -> socket.connect(target(), 5000))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("407 Proxy Authentication Required");
    assertThat(socket.isClosed()).isTrue();
    request.get(5, TimeUnit.SECONDS);
  }

  @Test
  void malformedResponse() throws Exception {
    CompletableFuture<String> request = serveOnce("garbage\r\n\r\n");
    GrpcH2cConnectTunnelSocketFactory factory =
        new GrpcH2cConnectTunnelSocketFactory(ProxyOptions.create(proxyAddress()), ENDPOINT);

    Socket socket = factory.createSocket();
    assertThatThrownBy(() -> socket.connect(target(), 5000))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("garbage");
    request.get(5, TimeUnit.SECONDS);
  }

  @Test
  void oversizedResponse() throws Exception {
    char[] filler = new char[10_000];
    Arrays.fill(filler, 'a');
    CompletableFuture<String> request =
        serveOnce("HTTP/1.1 200 OK\r\nX-Big: " + new String(filler) + "\r\n\r\n");
    GrpcH2cConnectTunnelSocketFactory factory =
        new GrpcH2cConnectTunnelSocketFactory(ProxyOptions.create(proxyAddress()), ENDPOINT);

    Socket socket = factory.createSocket();
    assertThatThrownBy(() -> socket.connect(target(), 5000))
        .isInstanceOf(IOException.class)
        .hasMessageContaining("too large");
    request.get(5, TimeUnit.SECONDS);
  }

  @Test
  void noHttpProxySelected_connectsDirectly() throws Exception {
    GrpcH2cConnectTunnelSocketFactory factory =
        new GrpcH2cConnectTunnelSocketFactory(ProxyOptions.noProxy(), ENDPOINT);
    assertThat(GrpcH2cConnectTunnelSocketFactory.selectsHttpProxy(ProxyOptions.noProxy(), ENDPOINT))
        .isFalse();

    CompletableFuture<Socket> accepted =
        CompletableFuture.supplyAsync(() -> {
          try {
            return proxyServer.accept();
          } catch (IOException e) {
            throw new IllegalStateException(e);
          }
        });
    try (Socket socket = factory.createSocket()) {
      socket.connect(
          InetSocketAddress.createUnresolved("127.0.0.1", proxyServer.getLocalPort()), 5000);
      assertThat(socket.isConnected()).isTrue();
    }
    accepted.get(5, TimeUnit.SECONDS).close();
  }
}
