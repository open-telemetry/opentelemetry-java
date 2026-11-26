/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;

/**
 * A minimal HTTP proxy for tests which only supports {@code CONNECT} tunneling, optionally
 * requiring Basic proxy authentication. Any other request is recorded and rejected with {@code 405
 * Method Not Allowed}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class TestHttpProxy implements Closeable {

  private final ServerSocket serverSocket;
  private final ExecutorService executor = Executors.newCachedThreadPool();
  @Nullable private final String expectedProxyAuthorization;
  private final ConcurrentLinkedQueue<ProxiedRequest> requests = new ConcurrentLinkedQueue<>();

  private TestHttpProxy(@Nullable String username, @Nullable String password) throws IOException {
    this.expectedProxyAuthorization =
        username == null || password == null
            ? null
            : "Basic "
                + Base64.getEncoder()
                    .encodeToString((username + ":" + password).getBytes(StandardCharsets.UTF_8));
    this.serverSocket = new ServerSocket(0);
    executor.execute(this::acceptLoop);
  }

  /** Starts a proxy which does not require authentication. */
  public static TestHttpProxy start() throws IOException {
    return new TestHttpProxy(null, null);
  }

  /** Starts a proxy which requires Basic authentication with {@code username:password}. */
  public static TestHttpProxy startWithBasicAuth(String username, String password)
      throws IOException {
    return new TestHttpProxy(username, password);
  }

  /** The proxy address. */
  public InetSocketAddress address() {
    return InetSocketAddress.createUnresolved("localhost", serverSocket.getLocalPort());
  }

  /** Requests the proxy received, in order, including rejected ones. */
  public List<ProxiedRequest> requests() {
    return Collections.unmodifiableList(new ArrayList<>(requests));
  }

  @Override
  public void close() throws IOException {
    serverSocket.close();
    executor.shutdownNow();
  }

  private void acceptLoop() {
    while (!serverSocket.isClosed()) {
      try {
        Socket client = serverSocket.accept();
        executor.execute(() -> handle(client));
      } catch (IOException e) {
        return;
      }
    }
  }

  private void handle(Socket client) {
    try {
      InputStream in = client.getInputStream();
      OutputStream out = client.getOutputStream();
      while (true) {
        byte[] head = readHead(in);
        if (head == null) {
          return;
        }
        String headString = new String(head, StandardCharsets.ISO_8859_1);
        String[] lines = headString.split("\r\n");
        String[] requestLine = lines[0].split(" ");
        String method = requestLine[0];
        String target = requestLine[1];
        String proxyAuthorization = null;
        for (int i = 1; i < lines.length; i++) {
          int colon = lines[i].indexOf(':');
          if (colon > 0
              && lines[i]
                  .substring(0, colon)
                  .trim()
                  .toLowerCase(Locale.ROOT)
                  .equals("proxy-authorization")) {
            proxyAuthorization = lines[i].substring(colon + 1).trim();
          }
        }
        requests.add(new ProxiedRequest(method, target, proxyAuthorization));

        if (!method.equals("CONNECT")) {
          out.write(
              ("HTTP/1.1 405 Method Not Allowed\r\n"
                      + "Allow: CONNECT\r\n"
                      + "Content-Length: 0\r\n"
                      + "Connection: close\r\n"
                      + "\r\n")
                  .getBytes(StandardCharsets.ISO_8859_1));
          out.flush();
          return;
        }
        if (expectedProxyAuthorization != null
            && !expectedProxyAuthorization.equals(proxyAuthorization)) {
          out.write(
              ("HTTP/1.1 407 Proxy Authentication Required\r\n"
                      + "Proxy-Authenticate: Basic realm=\"test\"\r\n"
                      + "Content-Length: 0\r\n"
                      + "\r\n")
                  .getBytes(StandardCharsets.ISO_8859_1));
          out.flush();
          // CONNECT requests have no body, so keep the connection open for the client to retry
          // with credentials.
          continue;
        }

        // CONNECT target is authority-form: host:port, with IPv6 hosts in brackets.
        int colon = target.lastIndexOf(':');
        String host = target.substring(0, colon);
        if (host.startsWith("[") && host.endsWith("]")) {
          host = host.substring(1, host.length() - 1);
        }
        try (Socket upstream = new Socket(host, Integer.parseInt(target.substring(colon + 1)))) {
          out.write(
              "HTTP/1.1 200 Connection established\r\n\r\n".getBytes(StandardCharsets.ISO_8859_1));
          out.flush();
          pipe(client, upstream);
        }
        return;
      }
    } catch (IOException e) {
      // Connection closed
    } finally {
      closeQuietly(client);
    }
  }

  private static void closeQuietly(Socket socket) {
    try {
      socket.close();
    } catch (IOException e) {
      // ignore
    }
  }

  private void pipe(Socket client, Socket upstream) throws IOException {
    OutputStream upstreamOut = upstream.getOutputStream();
    executor.execute(() -> {
      try {
        copy(upstream.getInputStream(), client.getOutputStream());
      } catch (IOException e) {
        // Connection closed
      }
    });
    copy(client.getInputStream(), upstreamOut);
  }

  private static void copy(InputStream in, OutputStream out) throws IOException {
    byte[] buffer = new byte[8192];
    int read;
    while ((read = in.read(buffer)) != -1) {
      out.write(buffer, 0, read);
      out.flush();
    }
  }

  /** Reads bytes up to and including the blank line ending the request head. */
  @Nullable
  private static byte[] readHead(InputStream in) throws IOException {
    ByteArrayOutputStream head = new ByteArrayOutputStream();
    int matched = 0;
    int b;
    while ((b = in.read()) != -1) {
      head.write(b);
      // Match "\r\n\r\n"
      if ((matched % 2 == 0 && b == '\r') || (matched % 2 == 1 && b == '\n')) {
        matched++;
        if (matched == 4) {
          return head.toByteArray();
        }
      } else {
        matched = b == '\r' ? 1 : 0;
      }
    }
    return null;
  }

  /** A request received by the proxy. */
  public static final class ProxiedRequest {
    private final String method;
    private final String target;
    @Nullable private final String proxyAuthorization;

    private ProxiedRequest(String method, String target, @Nullable String proxyAuthorization) {
      this.method = method;
      this.target = target;
      this.proxyAuthorization = proxyAuthorization;
    }

    public String method() {
      return method;
    }

    public String target() {
      return target;
    }

    @Nullable
    public String proxyAuthorization() {
      return proxyAuthorization;
    }

    @Override
    public String toString() {
      return method + " " + target + " (Proxy-Authorization=" + proxyAuthorization + ")";
    }
  }
}
