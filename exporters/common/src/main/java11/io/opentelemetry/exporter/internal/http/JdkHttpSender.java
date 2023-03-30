/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

public class JdkHttpSender implements HttpSender {

  private static final Set<Integer> retryableStatusCodes =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(429, 502, 503, 504)));

  private static final ThreadLocal<NoCopyByteArrayOutputStream> threadLocalBaos =
      ThreadLocal.withInitial(NoCopyByteArrayOutputStream::new);
  private static final ThreadLocal<ByteBufferPool> threadLocalByteBufPool =
      ThreadLocal.withInitial(ByteBufferPool::new);

  private final ExecutorService executorService = Executors.newFixedThreadPool(5);
  private final HttpClient client;
  private final URI uri;
  private final boolean compressionEnabled;
  private final Supplier<Map<String, String>> headerSupplier;
  private final RetryPolicyCopy retryPolicyCopy;

  JdkHttpSender(
      String endpoint,
      boolean compressionEnabled,
      Supplier<Map<String, String>> headerSupplier,
      @Nullable RetryPolicyCopy retryPolicyCopy,
      @Nullable X509TrustManager trustManager,
      @Nullable X509KeyManager keyManager) {
    HttpClient.Builder builder = HttpClient.newBuilder().executor(executorService);
    maybeConfigSsl(builder, trustManager, keyManager);
    this.client = builder.build();
    try {
      this.uri = new URI(endpoint);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    this.compressionEnabled = compressionEnabled;
    this.headerSupplier = headerSupplier;
    this.retryPolicyCopy =
        retryPolicyCopy == null
            ? new RetryPolicyCopy(1, Duration.ZERO, Duration.ZERO, 0)
            : retryPolicyCopy;
  }

  private static void maybeConfigSsl(
      HttpClient.Builder builder,
      @Nullable X509TrustManager trustManager,
      @Nullable X509KeyManager keyManager) {
    if (keyManager == null && trustManager == null) {
      return;
    }
    SSLContext context;
    try {
      // TODO: address
      context = SSLContext.getInstance("TLSv1.2");
      context.init(
          keyManager == null ? null : new KeyManager[] {keyManager},
          trustManager == null ? null : new TrustManager[] {trustManager},
          null);
    } catch (NoSuchAlgorithmException | KeyManagementException e) {
      throw new IllegalArgumentException(e);
    }
    builder.sslContext(context);
  }

  @Override
  public void send(
      Consumer<OutputStream> marshaler,
      int contentLength,
      Consumer<Response> onResponse,
      Consumer<Throwable> onError) {
    CompletableFuture<HttpResponse<byte[]>> unused =
        CompletableFuture.supplyAsync(() -> sendInternal(marshaler), executorService)
            .whenComplete(
                (httpResponse, throwable) -> {
                  if (throwable != null) {
                    onError.accept(throwable);
                    return;
                  }
                  onResponse.accept(toHttpResponse(httpResponse));
                });
  }

  private HttpResponse<byte[]> sendInternal(Consumer<OutputStream> marshaler) {
    HttpRequest.Builder requestBuilder = HttpRequest.newBuilder().uri(uri);
    headerSupplier.get().forEach(requestBuilder::setHeader);
    requestBuilder.header("Content-Type", "application/x-protobuf");

    NoCopyByteArrayOutputStream os = threadLocalBaos.get();
    os.reset();
    if (compressionEnabled) {
      requestBuilder.header("Content-Encoding", "gzip");
      try (GZIPOutputStream gzos = new GZIPOutputStream(os)) {
        marshaler.accept(gzos);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    } else {
      marshaler.accept(os);
    }

    ByteBufferPool byteBufferPool = threadLocalByteBufPool.get();
    requestBuilder.POST(new BodyPublisher(os.buf(), os.size(), byteBufferPool::getBuffer));

    long attempt = 0;
    long nextBackoffNanos = retryPolicyCopy.initialBackoff.toNanos();
    do {
      try {
        HttpResponse<byte[]> httpResponse =
            client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
        byteBufferPool.resetPool();

        attempt++;
        if (attempt >= retryPolicyCopy.maxAttempts
            || !retryableStatusCodes.contains(httpResponse.statusCode())) {
          return httpResponse;
        }

        // Compute and sleep for backoff
        long upperBoundNanos = Math.min(nextBackoffNanos, retryPolicyCopy.maxBackoff.toNanos());
        long backoffNanos = ThreadLocalRandom.current().nextLong(upperBoundNanos);
        nextBackoffNanos = (long) (nextBackoffNanos * retryPolicyCopy.backoffMultiplier);
        try {
          TimeUnit.NANOSECONDS.sleep(backoffNanos);
        } catch (InterruptedException e) {
          throw new IllegalStateException(e);
        }
      } catch (IOException | InterruptedException e) {
        // TODO: is throwable retryable?
        throw new IllegalStateException(e);
      }
    } while (true);
  }

  private static class NoCopyByteArrayOutputStream extends ByteArrayOutputStream {
    NoCopyByteArrayOutputStream() {
      super(retryableStatusCodes.size());
    }

    private byte[] buf() {
      return buf;
    }
  }

  private static Response toHttpResponse(HttpResponse<byte[]> response) {
    return new Response() {
      @Override
      public int statusCode() {
        return response.statusCode();
      }

      @Override
      public String statusMessage() {
        return String.valueOf(response.statusCode());
      }

      @Override
      public byte[] responseBody() {
        return response.body();
      }
    };
  }

  private static class ByteBufferPool {

    // TODO: make configurable?
    private static final int BUF_SIZE = 16 * 1024;

    private final ConcurrentLinkedQueue<ByteBuffer> pool = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<ByteBuffer> out = new ConcurrentLinkedQueue<>();

    private ByteBuffer getBuffer() {
      ByteBuffer buffer = pool.poll();
      if (buffer == null) {
        buffer = ByteBuffer.allocate(BUF_SIZE);
      }
      out.offer(buffer);
      return buffer;
    }

    private void resetPool() {
      ByteBuffer buf = out.poll();
      while (buf != null) {
        pool.offer(buf);
        buf = out.poll();
      }
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    executorService.shutdown();
    return CompletableResultCode.ofSuccess();
  }
}
