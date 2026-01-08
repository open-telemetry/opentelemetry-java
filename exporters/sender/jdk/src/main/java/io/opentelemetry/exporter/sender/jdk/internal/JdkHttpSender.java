/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.jdk.internal;

import static java.util.stream.Collectors.joining;

import io.opentelemetry.exporter.compressor.Compressor;
import io.opentelemetry.exporter.http.HttpSender;
import io.opentelemetry.exporter.marshal.MessageWriter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;

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

  private static final Logger logger = Logger.getLogger(JdkHttpSender.class.getName());

  private final boolean managedExecutor;
  private final ExecutorService executorService;
  private final HttpClient client;
  private final URI endpoint;
  private final String contentType;
  @Nullable private final Compressor compressor;
  private final long timeoutNanos;
  private final Supplier<Map<String, List<String>>> headerSupplier;
  @Nullable private final RetryPolicy retryPolicy;
  private final Predicate<IOException> retryExceptionPredicate;

  // Visible for testing
  JdkHttpSender(
      HttpClient client,
      URI endpoint,
      String contentType,
      @Nullable Compressor compressor,
      long timeoutNanos,
      Supplier<Map<String, List<String>>> headerSupplier,
      @Nullable RetryPolicy retryPolicy,
      @Nullable ExecutorService executorService) {
    this.client = client;
    this.endpoint = endpoint;
    this.contentType = contentType;
    this.compressor = compressor;
    this.timeoutNanos = timeoutNanos;
    this.headerSupplier = headerSupplier;
    this.retryPolicy = retryPolicy;
    this.retryExceptionPredicate =
        Optional.ofNullable(retryPolicy)
            .map(RetryPolicy::getRetryExceptionPredicate)
            .orElse(JdkHttpSender::isRetryableException);
    if (executorService == null) {
      this.executorService = newExecutor();
      this.managedExecutor = true;
    } else {
      this.executorService = executorService;
      this.managedExecutor = false;
    }
  }

  JdkHttpSender(
      URI endpoint,
      String contentType,
      @Nullable Compressor compressor,
      long timeoutNanos,
      long connectTimeoutNanos,
      Supplier<Map<String, List<String>>> headerSupplier,
      @Nullable RetryPolicy retryPolicy,
      @Nullable ProxyOptions proxyOptions,
      @Nullable SSLContext sslContext,
      @Nullable ExecutorService executorService) {
    this(
        configureClient(sslContext, connectTimeoutNanos, proxyOptions),
        endpoint,
        contentType,
        compressor,
        timeoutNanos,
        headerSupplier,
        retryPolicy,
        executorService);
  }

  private static ExecutorService newExecutor() {
    return new ThreadPoolExecutor(
        0,
        Integer.MAX_VALUE,
        60,
        TimeUnit.SECONDS,
        new SynchronousQueue<>(),
        new DaemonThreadFactory("jdkhttp-executor"));
  }

  private static HttpClient configureClient(
      @Nullable SSLContext sslContext,
      long connectionTimeoutNanos,
      @Nullable ProxyOptions proxyOptions) {
    HttpClient.Builder builder =
        HttpClient.newBuilder().connectTimeout(Duration.ofNanos(connectionTimeoutNanos));
    if (sslContext != null) {
      builder.sslContext(sslContext);
    }
    if (proxyOptions != null) {
      builder.proxy(proxyOptions.getProxySelector());
    }
    return builder.build();
  }

  @Override
  public void send(
      MessageWriter messageWriter,
      Consumer<io.opentelemetry.exporter.http.HttpResponse> onResponse,
      Consumer<Throwable> onError) {
    CompletableFuture<HttpResponse<byte[]>> unused =
        CompletableFuture.supplyAsync(
                () -> {
                  try {
                    return sendInternal(messageWriter);
                  } catch (IOException e) {
                    throw new UncheckedIOException(e);
                  }
                },
                executorService)
            .whenComplete(
                (httpResponse, throwable) -> {
                  if (throwable != null) {
                    onError.accept(throwable);
                    return;
                  }
                  onResponse.accept(toHttpResponse(httpResponse));
                });
  }

  // Visible for testing
  HttpResponse<byte[]> sendInternal(MessageWriter requestBodyWriter) throws IOException {
    long startTimeNanos = System.nanoTime();
    HttpRequest.Builder requestBuilder =
        HttpRequest.newBuilder().uri(endpoint).timeout(Duration.ofNanos(timeoutNanos));
    Map<String, List<String>> headers = headerSupplier.get();
    if (headers != null) {
      headers.forEach((key, values) -> values.forEach(value -> requestBuilder.header(key, value)));
    }
    requestBuilder.header("Content-Type", contentType);

    NoCopyByteArrayOutputStream os = threadLocalBaos.get();
    os.reset();
    if (compressor != null) {
      requestBuilder.header("Content-Encoding", compressor.getEncoding());
      try (OutputStream compressed = compressor.compress(os)) {
        requestBodyWriter.writeMessage(compressed);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
    } else {
      requestBodyWriter.writeMessage(os);
    }

    ByteBufferPool byteBufferPool = threadLocalByteBufPool.get();
    requestBuilder.POST(new BodyPublisher(os.buf(), os.size(), byteBufferPool::getBuffer));

    // If no retry policy, short circuit
    if (retryPolicy == null) {
      return sendRequest(requestBuilder, byteBufferPool);
    }

    long attempt = 0;
    long nextBackoffNanos = retryPolicy.getInitialBackoff().toNanos();
    HttpResponse<byte[]> httpResponse = null;
    IOException exception = null;
    do {
      if (attempt > 0) {
        // Compute and sleep for backoff
        long currentBackoffNanos =
            Math.min(nextBackoffNanos, retryPolicy.getMaxBackoff().toNanos());
        long backoffNanos =
            (long) (ThreadLocalRandom.current().nextDouble(0.8d, 1.2d) * currentBackoffNanos);
        nextBackoffNanos = (long) (currentBackoffNanos * retryPolicy.getBackoffMultiplier());
        try {
          TimeUnit.NANOSECONDS.sleep(backoffNanos);
        } catch (InterruptedException e) {
          Thread.currentThread().interrupt();
          break; // Break out and return response or throw
        }
        // If after sleeping we've exceeded timeoutNanos, break out and return
        // response or throw
        if ((System.nanoTime() - startTimeNanos) >= timeoutNanos) {
          break;
        }
      }
      httpResponse = null;
      exception = null;
      requestBuilder.timeout(Duration.ofNanos(timeoutNanos - (System.nanoTime() - startTimeNanos)));
      try {
        httpResponse = sendRequest(requestBuilder, byteBufferPool);
        boolean retryable = retryableStatusCodes.contains(httpResponse.statusCode());
        if (logger.isLoggable(Level.FINER)) {
          logger.log(
              Level.FINER,
              "Attempt "
                  + attempt
                  + " returned "
                  + (retryable ? "retryable" : "non-retryable")
                  + " response: "
                  + responseStringRepresentation(httpResponse));
        }
        if (!retryable) {
          return httpResponse;
        }
      } catch (IOException e) {
        exception = e;
        boolean retryable = retryExceptionPredicate.test(exception);
        if (logger.isLoggable(Level.FINER)) {
          logger.log(
              Level.FINER,
              "Attempt "
                  + attempt
                  + " failed with "
                  + (retryable ? "retryable" : "non-retryable")
                  + " exception",
              exception);
        }
        if (!retryable) {
          throw exception;
        }
      }
    } while (++attempt < retryPolicy.getMaxAttempts());

    if (httpResponse != null) {
      return httpResponse;
    }
    throw exception;
  }

  private static String responseStringRepresentation(HttpResponse<?> response) {
    StringJoiner joiner = new StringJoiner(",", "HttpResponse{", "}");
    joiner.add("code=" + response.statusCode());
    joiner.add(
        "headers="
            + response.headers().map().entrySet().stream()
                .map(entry -> entry.getKey() + "=" + String.join(",", entry.getValue()))
                .collect(joining(",", "[", "]")));
    return joiner.toString();
  }

  private HttpResponse<byte[]> sendRequest(
      HttpRequest.Builder requestBuilder, ByteBufferPool byteBufferPool) throws IOException {
    try {
      return client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofByteArray());
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException(e);
    } finally {
      byteBufferPool.resetPool();
    }
  }

  private static boolean isRetryableException(IOException throwable) {
    // Almost all IOExceptions we've encountered are transient retryable, so we
    // opt out of specific
    // IOExceptions that are unlikely to resolve rather than opting in.
    // Known retryable IOException messages: "Connection reset", "/{remote
    // ip}:{remote port} GOAWAY
    // received"
    // Known retryable HttpTimeoutException messages: "request timed out"
    // Known retryable HttpConnectTimeoutException messages: "HTTP connect timed
    // out"
    return !(throwable instanceof SSLException);
  }

  private static class NoCopyByteArrayOutputStream extends ByteArrayOutputStream {
    NoCopyByteArrayOutputStream() {
      super(retryableStatusCodes.size());
    }

    private byte[] buf() {
      return buf;
    }
  }

  private static io.opentelemetry.exporter.http.HttpResponse toHttpResponse(
      HttpResponse<byte[]> response) {
    return new io.opentelemetry.exporter.http.HttpResponse() {
      @Override
      public int getStatusCode() {
        return response.statusCode();
      }

      @Override
      public String getStatusMessage() {
        return String.valueOf(response.statusCode());
      }

      @Override
      public byte[] getResponseBody() {
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
    if (managedExecutor) {
      executorService.shutdown();
    }
    if (AutoCloseable.class.isInstance(client)) {
      try {
        AutoCloseable.class.cast(client).close();
      } catch (Exception e) {
        return CompletableResultCode.ofExceptionalFailure(e);
      }
    }
    return CompletableResultCode.ofSuccess();
  }
}
