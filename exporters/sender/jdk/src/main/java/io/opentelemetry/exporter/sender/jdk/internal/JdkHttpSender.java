/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.jdk.internal;

import io.opentelemetry.exporter.internal.http.HttpSender;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
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
import javax.net.ssl.SSLContext;

/**
 * {@link HttpSender} which is backed by JDK {@link HttpClient}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class JdkHttpSender implements HttpSender {

  private static final Set<Integer> retryableStatusCodes = Set.of(429, 502, 503, 504);

  private static final ThreadLocal<NoCopyByteArrayOutputStream> threadLocalBaos =
      ThreadLocal.withInitial(NoCopyByteArrayOutputStream::new);
  private static final ThreadLocal<ByteBufferPool> threadLocalByteBufPool =
      ThreadLocal.withInitial(ByteBufferPool::new);

  private final ExecutorService executorService = Executors.newFixedThreadPool(5);
  private final HttpClient client;
  private final URI uri;
  private final boolean compressionEnabled;
  private final String contentType;
  private final long timeoutNanos;
  private final Supplier<Map<String, String>> headerSupplier;
  @Nullable private final RetryPolicy retryPolicy;

  JdkHttpSender(
      String endpoint,
      boolean compressionEnabled,
      String contentType,
      long timeoutNanos,
      Supplier<Map<String, String>> headerSupplier,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext) {
    HttpClient.Builder builder = HttpClient.newBuilder().executor(executorService);
    if (sslContext != null) {
      builder.sslContext(sslContext);
    }
    this.client = builder.build();
    try {
      this.uri = new URI(endpoint);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException(e);
    }
    this.compressionEnabled = compressionEnabled;
    this.contentType = contentType;
    this.timeoutNanos = timeoutNanos;
    this.headerSupplier = headerSupplier;
    this.retryPolicy = retryPolicy;
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
    long startTimeNanos = System.nanoTime();
    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder().uri(uri).timeout(Duration.ofNanos(timeoutNanos));
    headerSupplier.get().forEach(requestBuilder::setHeader);
    requestBuilder.header("Content-Type", contentType);

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

    // If no retry policy, short circuit
    if (retryPolicy == null) {
      return sendRequest(requestBuilder, byteBufferPool);
    }

    long attempt = 0;
    long nextBackoffNanos = retryPolicy.getInitialBackoff().toNanos();
    do {
      requestBuilder.timeout(Duration.ofNanos(timeoutNanos - (System.nanoTime() - startTimeNanos)));
      HttpResponse<byte[]> httpResponse = sendRequest(requestBuilder, byteBufferPool);
      attempt++;
      if (attempt >= retryPolicy.getMaxAttempts()
          || !retryableStatusCodes.contains(httpResponse.statusCode())) {
        return httpResponse;
      }

      // Compute and sleep for backoff
      long upperBoundNanos = Math.min(nextBackoffNanos, retryPolicy.getMaxBackoff().toNanos());
      long backoffNanos = ThreadLocalRandom.current().nextLong(upperBoundNanos);
      nextBackoffNanos = (long) (nextBackoffNanos * retryPolicy.getBackoffMultiplier());
      try {
        TimeUnit.NANOSECONDS.sleep(backoffNanos);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IllegalStateException(e);
      }
      if ((System.nanoTime() - startTimeNanos) >= timeoutNanos) {
        return httpResponse;
      }
    } while (true);
  }

  private HttpResponse<byte[]> sendRequest(
      HttpRequest.Builder requestBuilder, ByteBufferPool byteBufferPool) {
    try {
      return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
    } catch (IOException | InterruptedException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt();
      }
      // TODO: is throwable retryable?
      throw new IllegalStateException(e);
    } finally {
      byteBufferPool.resetPool();
    }
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
