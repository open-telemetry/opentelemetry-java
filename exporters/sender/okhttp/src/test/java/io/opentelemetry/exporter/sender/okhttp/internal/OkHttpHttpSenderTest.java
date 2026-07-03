/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.sdk.common.export.HttpResponse;
import io.opentelemetry.sdk.common.export.MessageWriter;
import java.io.OutputStream;
import java.net.URI;
import java.security.Security;
import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import org.junit.jupiter.api.Test;

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
            Long.MAX_VALUE);

    AtomicReference<HttpResponse> responseRef = new AtomicReference<>();
    AtomicReference<Throwable> errorRef = new AtomicReference<>();

    sender.send(new NoOpRequestBodyWriter(), responseRef::set, errorRef::set);

    assertThat(errorRef.get()).isNotNull();
    assertThat(responseRef.get()).isNull();
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

    OkHttpHttpSender sender =
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
            Long.MAX_VALUE);

    assertThat(sender).isNotNull();
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
                      Long.MAX_VALUE))
          .isInstanceOf(IllegalStateException.class)
          .hasMessage("Unable to initialize default trust manager")
          .hasCauseInstanceOf(SSLException.class);

    } finally {
      Security.setProperty("ssl.TrustManagerFactory.algorithm", originalAlgorithm);
    }
  }
}
