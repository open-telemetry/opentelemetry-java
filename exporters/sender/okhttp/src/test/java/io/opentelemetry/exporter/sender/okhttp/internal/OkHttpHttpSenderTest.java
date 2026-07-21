/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.linecorp.armeria.testing.junit5.server.SelfSignedCertificateExtension;
import io.opentelemetry.exporter.internal.TlsUtil;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.HttpResponse;
import io.opentelemetry.sdk.common.export.MessageWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.Certificate;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class OkHttpHttpSenderTest {

  @Test
  void send_rejectedExecution_callsOnError() {
    ThreadPoolExecutor executor =
        new ThreadPoolExecutor(0, 1, 0, TimeUnit.SECONDS, new SynchronousQueue<>());
    executor.shutdown();

    OkHttpHttpSender sender =
        new OkHttpHttpSender(
            URI.create("http://localhost"),
            "text/plain",
            null,
            Duration.ofSeconds(10),
            Duration.ofSeconds(10),
            Collections::emptyMap,
            null,
            null,
            null,
            null,
            executor,
            Long.MAX_VALUE,
            null);

    AtomicReference<HttpResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    sender.send(new NoOpRequestBodyWriter(), responseRef::set, errorRef::set);

    assertThat(errorRef.get()).isNotNull();
    assertThat(responseRef.get()).isNull();
  }

  @Test
  void shutdown_CompletableResultCodeShouldWaitForThreads() throws Exception {
    // This test verifies that shutdown() returns a CompletableResultCode that only
    // completes AFTER threads terminate, not immediately.

    // Allocate an ephemeral port and immediately close it to get a port with nothing listening
    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }

    OkHttpHttpSender sender =
        newSender("http://localhost:" + port, null); // null executor = managed

    CompletableResultCode sendResult = new CompletableResultCode();
    sender.send(
        new NoOpRequestBodyWriter(), response -> sendResult.succeed(), error -> sendResult.fail());

    // Give threads time to start
    Thread.sleep(500);

    CompletableResultCode shutdownResult = sender.shutdown();

    // The CompletableResultCode should NOT be done() immediately because we need to wait for
    // threads to terminate.
    assertFalse(
        shutdownResult.isDone(),
        "CompletableResultCode should not be done immediately - it should wait for thread termination");

    shutdownResult.join(10, TimeUnit.SECONDS);
    assertTrue(shutdownResult.isDone(), "CompletableResultCode should be done after waiting");
    assertTrue(shutdownResult.isSuccess(), "Shutdown should complete successfully");
  }

  @Test
  void shutdown_NonManagedExecutor_ReturnsImmediately() {
    // This test verifies that when using a non-managed executor (custom ExecutorService),
    // shutdown() returns an already-completed CompletableResultCode immediately.

    ExecutorService customExecutor = Executors.newSingleThreadExecutor();

    try {
      OkHttpHttpSender sender = newSender("http://localhost:8080", customExecutor);

      CompletableResultCode shutdownResult = sender.shutdown();

      assertTrue(
          shutdownResult.isDone(),
          "CompletableResultCode should be done immediately for non-managed executor");
      assertTrue(shutdownResult.isSuccess(), "Shutdown should complete successfully");
    } finally {
      customExecutor.shutdownNow();
    }
  }

  @Test
  void shutdown_ExecutorDoesNotTerminateInTime_LogsWarningButSucceeds() throws Exception {
    // This test verifies that when threads don't terminate within 5 seconds, a warning is logged
    // but shutdown still succeeds.

    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }

    OkHttpHttpSender sender =
        newSender("http://localhost:" + port, null); // null executor = managed

    // Start multiple requests with callbacks that block longer than the 5-second timeout
    CountDownLatch blockCallbacks = new CountDownLatch(1);
    for (int i = 0; i < 3; i++) {
      sender.send(
          new NoOpRequestBodyWriter(),
          response -> {
            try {
              blockCallbacks.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          },
          error -> {
            try {
              blockCallbacks.await(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
              Thread.currentThread().interrupt();
            }
          });
    }

    Thread.sleep(500);

    CompletableResultCode shutdownResult = sender.shutdown();

    assertTrue(
        shutdownResult.join(10, TimeUnit.SECONDS).isSuccess(),
        "Shutdown should succeed even when threads don't terminate quickly");

    blockCallbacks.countDown();
  }

  @Test
  void shutdown_InterruptedWhileWaiting_StillSucceeds() throws Exception {
    // This test verifies that if the shutdown thread is interrupted while waiting for termination,
    // it still marks the shutdown as successful.

    int port;
    try (ServerSocket socket = new ServerSocket(0)) {
      port = socket.getLocalPort();
    }

    OkHttpHttpSender sender =
        newSender("http://localhost:" + port, null); // null executor = managed

    sender.send(new NoOpRequestBodyWriter(), response -> {}, error -> {});

    Thread.sleep(500);

    CompletableResultCode shutdownResult = sender.shutdown();

    // Give the shutdown thread a moment to start
    Thread.sleep(100);

    // Find and interrupt the okhttp-shutdown thread to trigger the InterruptedException path
    Thread[] threads = new Thread[Thread.activeCount() + 10];
    int count = Thread.enumerate(threads);
    for (int i = 0; i < count; i++) {
      Thread thread = threads[i];
      if (thread != null && thread.getName().equals("okhttp-shutdown")) {
        thread.interrupt();
        break;
      }
    }

    assertTrue(
        shutdownResult.join(10, TimeUnit.SECONDS).isSuccess(),
        "Shutdown should succeed even when interrupted");
  }

  @RegisterExtension
  static final SelfSignedCertificateExtension certificate = new SelfSignedCertificateExtension();

  @Test
  void enabledProtocols_legacyTls() throws Exception {
    SSLSocket probe = (SSLSocket) SSLSocketFactory.getDefault().createSocket();
    assertThat(probe.getEnabledProtocols())
        .as("TLSv1.1 must be enabled via enable-legacy-tls.security")
        .contains("TLSv1.1");
    probe.close();

    // Plain JDK SSLServerSocket: no BoringSSL, so TLSv1.1 is honored.
    KeyStore ks = KeyStore.getInstance("PKCS12");
    ks.load(null);
    ks.setKeyEntry(
        "key",
        certificate.privateKey(),
        new char[0],
        new Certificate[] {certificate.certificate()});
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
    kmf.init(ks, new char[0]);
    SSLContext serverCtx = SSLContext.getInstance("TLS");
    serverCtx.init(kmf.getKeyManagers(), null, null);

    try (SSLServerSocket serverSocket =
        (SSLServerSocket) serverCtx.getServerSocketFactory().createServerSocket(0)) {
      serverSocket.setEnabledProtocols(new String[] {"TLSv1.1"});
      int port = serverSocket.getLocalPort();

      Thread serverThread =
          new Thread(
              () -> {
                try (SSLSocket conn = (SSLSocket) serverSocket.accept()) {
                  InputStream is = conn.getInputStream();
                  OutputStream os = conn.getOutputStream();
                  // Read until end of HTTP headers.
                  StringBuilder sb = new StringBuilder();
                  while (!sb.toString().endsWith("\r\n\r\n")) {
                    int b = is.read();
                    if (b < 0) {
                      break;
                    }
                    sb.append((char) b);
                  }
                  os.write(
                      "HTTP/1.1 200 OK\r\nContent-Length: 0\r\n\r\n"
                          .getBytes(StandardCharsets.US_ASCII));
                  os.flush();
                } catch (Exception e) {
                  throw new RuntimeException(e);
                }
              });
      serverThread.setDaemon(true);
      serverThread.start();

      X509TrustManager trustManager = TlsUtil.trustManager(certificate.certificate().getEncoded());
      SSLContext clientCtx = SSLContext.getInstance("TLS");
      clientCtx.init(null, new TrustManager[] {trustManager}, null);

      OkHttpHttpSender sender =
          new OkHttpHttpSender(
              URI.create("https://localhost:" + port + "/"),
              "text/plain",
              null,
              Duration.ofSeconds(10),
              Duration.ofSeconds(10),
              Collections::emptyMap,
              null,
              null,
              clientCtx,
              trustManager,
              null,
              Long.MAX_VALUE,
              Collections.singletonList("TLSv1.1"));

      AtomicReference<HttpResponse> responseRef = new AtomicReference<>();
      AtomicReference<Throwable> errorRef = new AtomicReference<>();
      CountDownLatch latch = new CountDownLatch(1);
      sender.send(
          new NoOpRequestBodyWriter(),
          response -> {
            responseRef.set(response);
            latch.countDown();
          },
          error -> {
            errorRef.set(error);
            latch.countDown();
          });

      assertThat(latch.await(10, TimeUnit.SECONDS)).isTrue();
      assertThat(errorRef.get()).isNull();
      assertThat(responseRef.get()).isNotNull();
      assertThat(responseRef.get().getStatusCode()).isEqualTo(200);
    }
  }

  private static OkHttpHttpSender newSender(
      String endpoint, @Nullable ExecutorService executorService) {
    return new OkHttpHttpSender(
        URI.create(endpoint),
        "text/plain",
        null,
        Duration.ofSeconds(10),
        Duration.ofSeconds(10),
        Collections::emptyMap,
        null,
        null,
        null,
        null,
        executorService,
        Long.MAX_VALUE,
        null);
  }

  private static class NoOpRequestBodyWriter implements MessageWriter {
    @Override
    public void writeMessage(OutputStream output) {}

    @Override
    public int getContentLength() {
      return 0;
    }
  }

  @Test
  void constructor_usesDefaultTrustManagerWhenTrustManagerIsNull() throws Exception {
    SSLContext sslContext = SSLContext.getInstance("TLS");
    sslContext.init(null, null, null);

    assertThatCode(
            () ->
                new OkHttpHttpSender(
                    URI.create("https://localhost"),
                    "text/plain",
                    null,
                    Duration.ofSeconds(10),
                    Duration.ofSeconds(10),
                    Collections::emptyMap,
                    null,
                    null,
                    sslContext,
                    null,
                    null,
                    Long.MAX_VALUE,
                    null))
        .doesNotThrowAnyException();
  }

  @Test
  void constructor_wrapsDefaultTrustManagerFailure() throws Exception {
    String originalAlgorithm = Security.getProperty("ssl.TrustManagerFactory.algorithm");

    try {
      Security.setProperty("ssl.TrustManagerFactory.algorithm", "invalid");

      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, null, null);

      assertThatThrownBy(
              () ->
                  new OkHttpHttpSender(
                      URI.create("https://localhost"),
                      "text/plain",
                      null,
                      Duration.ofSeconds(10),
                      Duration.ofSeconds(10),
                      Collections::emptyMap,
                      null,
                      null,
                      sslContext,
                      null,
                      null,
                      Long.MAX_VALUE,
                      null))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Unable to initialize default trust manager")
          .hasCauseInstanceOf(SSLException.class);

    } finally {
      Security.setProperty("ssl.TrustManagerFactory.algorithm", originalAlgorithm);
    }
  }
}
