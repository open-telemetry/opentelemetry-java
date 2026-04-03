/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.api.internal.InstrumentationUtil;
import io.opentelemetry.exporter.internal.RetryUtil;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.Compressor;
import io.opentelemetry.sdk.common.export.HttpResponse;
import io.opentelemetry.sdk.common.export.HttpSender;
import io.opentelemetry.sdk.common.export.MessageWriter;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.ConnectionSpec;
import okhttp3.Dispatcher;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.GzipSource;
import okio.Okio;
import okio.Source;

/**
 * {@link HttpSender} which is backed by OkHttp.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OkHttpHttpSender implements HttpSender {

  private static final Logger logger = Logger.getLogger(OkHttpHttpSender.class.getName());

  private final boolean managedExecutor;
  private final OkHttpClient client;
  private final HttpUrl url;
  private final Supplier<Map<String, List<String>>> headerSupplier;
  private final MediaType mediaType;
  @Nullable private final Compressor compressor;
  private final long maxResponseBodySize;

  /** Create a sender. */
  @SuppressWarnings("TooManyParameters")
  public OkHttpHttpSender(
      URI endpoint,
      String contentType,
      @Nullable Compressor compressor,
      Duration timeout,
      Duration connectTimeout,
      Supplier<Map<String, List<String>>> headerSupplier,
      @Nullable ProxyOptions proxyOptions,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager,
      @Nullable ExecutorService executorService,
      long maxResponseBodySize) {
    int callTimeoutMillis = (int) Math.min(timeout.toMillis(), Integer.MAX_VALUE);
    int connectTimeoutMillis = (int) Math.min(connectTimeout.toMillis(), Integer.MAX_VALUE);

    Dispatcher dispatcher;
    if (executorService == null) {
      dispatcher = OkHttpUtil.newDispatcher();
      this.managedExecutor = true;
    } else {
      dispatcher = new Dispatcher(executorService);
      this.managedExecutor = false;
    }

    OkHttpClient.Builder builder =
        new OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .connectTimeout(Duration.ofMillis(connectTimeoutMillis))
            .callTimeout(Duration.ofMillis(callTimeoutMillis));

    if (proxyOptions != null) {
      builder.proxySelector(proxyOptions.getProxySelector());
    }

    if (retryPolicy != null) {
      builder.addInterceptor(new RetryInterceptor(retryPolicy, OkHttpHttpSender::isRetryable));
    }

    boolean isPlainHttp = endpoint.getScheme().equals("http");
    if (isPlainHttp) {
      builder.connectionSpecs(Collections.singletonList(ConnectionSpec.CLEARTEXT));
    } else if (sslContext != null && trustManager != null) {
      builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
    }

    this.client = builder.build();
    this.url = HttpUrl.get(endpoint);
    this.mediaType = MediaType.parse(contentType);
    this.compressor = compressor;
    this.headerSupplier = headerSupplier;
    this.maxResponseBodySize = maxResponseBodySize;
  }

  @Override
  public void send(
      MessageWriter messageWriter, Consumer<HttpResponse> onResponse, Consumer<Throwable> onError) {
    Request.Builder requestBuilder = new Request.Builder().url(url);

    Map<String, List<String>> headers = headerSupplier.get();
    if (headers != null) {
      headers.forEach(
          (key, values) -> values.forEach(value -> requestBuilder.addHeader(key, value)));
    }
    if (compressor != null) {
      requestBuilder.addHeader("Content-Encoding", compressor.getEncoding());
    }
    // Explicitly advertise gzip and identity encoding support. Because we set Accept-Encoding
    // ourselves, OkHttp's BridgeInterceptor will not transparently decompress gzip responses
    // (it only does so when it added the header), so we handle decompression ourselves below.
    requestBuilder.addHeader("Accept-Encoding", "gzip, identity");
    requestBuilder.post(new RequestBodyImpl(messageWriter, compressor, mediaType));

    InstrumentationUtil.suppressInstrumentation(
        () ->
            client
                .newCall(requestBuilder.build())
                .enqueue(
                    new Callback() {
                      @Override
                      public void onFailure(Call call, IOException e) {
                        onError.accept(e);
                      }

                      @Override
                      public void onResponse(Call call, Response response) {
                        handleResponse(response, onResponse, onError);
                      }
                    }));
  }

  private void handleResponse(
      Response response, Consumer<HttpResponse> onResponse, Consumer<Throwable> onError) {
    try (ResponseBody body = response.body()) {
      String contentEncoding = response.header("Content-Encoding");
      if (contentEncoding != null && !"gzip".equalsIgnoreCase(contentEncoding)) {
        onError.accept(new IOException("Unsupported Content-Encoding: " + contentEncoding));
        return;
      }
      boolean decompress = "gzip".equalsIgnoreCase(contentEncoding);
      // Read up to maxResponseBodySize + 1 bytes. Reading exactly one byte more than the limit
      // lets us detect overflow: if the buffer ends up larger than maxResponseBodySize, the body
      // exceeded the limit. A body exactly at the limit will only fill the buffer to
      // maxResponseBodySize (EOF is reached before the extra byte is read).
      // If maxResponseBodySize is Long.MAX_VALUE, adding 1 would overflow. In that case use
      // Long.MAX_VALUE directly — the overflow check can never trigger for such a large limit.
      long readUpTo =
          maxResponseBodySize == Long.MAX_VALUE ? Long.MAX_VALUE : maxResponseBodySize + 1;
      Buffer buffer = new Buffer();
      try {
        Source source = decompress ? new GzipSource(body.source()) : body.source();
        while (buffer.size() <= maxResponseBodySize) {
          long n = source.read(buffer, readUpTo - buffer.size());
          if (n == -1L) {
            break;
          }
        }
      } catch (IOException e) {
        logger.log(Level.WARNING, "Failed to read response body", e);
      }

      if (buffer.size() > maxResponseBodySize) {
        onError.accept(
            new IOException(
                "HTTP response body exceeded limit of " + maxResponseBodySize + " bytes"));
        return;
      }

      byte[] bodyBytes = buffer.readByteArray();
      onResponse.accept(
          new HttpResponse() {
            @Override
            public int getStatusCode() {
              return response.code();
            }

            @Override
            public String getStatusMessage() {
              return response.message();
            }

            @Override
            public byte[] getResponseBody() {
              return bodyBytes;
            }
          });
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    client.dispatcher().cancelAll();
    if (managedExecutor) {
      client.dispatcher().executorService().shutdownNow();
    }
    client.connectionPool().evictAll();
    return CompletableResultCode.ofSuccess();
  }

  static boolean isRetryable(Response response) {
    return RetryUtil.retryableHttpResponseCodes().contains(response.code());
  }

  private static class RequestBodyImpl extends RequestBody {

    private final MessageWriter requestBodyWriter;
    @Nullable private final Compressor compressor;
    private final MediaType mediaType;

    private RequestBodyImpl(
        MessageWriter requestBodyWriter, @Nullable Compressor compressor, MediaType mediaType) {
      this.requestBodyWriter = requestBodyWriter;
      this.compressor = compressor;
      this.mediaType = mediaType;
    }

    @Override
    public long contentLength() {
      return compressor == null ? requestBodyWriter.getContentLength() : -1;
    }

    @Override
    public MediaType contentType() {
      return mediaType;
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
      if (compressor != null) {
        BufferedSink compressedSink =
            Okio.buffer(Okio.sink(compressor.compress(bufferedSink.outputStream())));
        requestBodyWriter.writeMessage(compressedSink.outputStream());
        compressedSink.close();
      } else {
        requestBodyWriter.writeMessage(bufferedSink.outputStream());
      }
    }
  }
}
