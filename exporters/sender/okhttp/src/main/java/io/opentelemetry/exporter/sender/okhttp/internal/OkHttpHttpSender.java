/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.api.internal.InstrumentationUtil;
import io.opentelemetry.exporter.internal.RetryUtil;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.http.HttpSender;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;
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
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.Okio;

/**
 * {@link HttpSender} which is backed by OkHttp.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OkHttpHttpSender implements HttpSender {

  private final boolean managedExecutor;
  private final OkHttpClient client;
  private final HttpUrl url;
  @Nullable private final Compressor compressor;
  private final boolean exportAsJson;
  private final Supplier<Map<String, List<String>>> headerSupplier;
  private final MediaType mediaType;

  /** Create a sender. */
  @SuppressWarnings("TooManyParameters")
  public OkHttpHttpSender(
      String endpoint,
      @Nullable Compressor compressor,
      boolean exportAsJson,
      String contentType,
      long timeoutNanos,
      long connectionTimeoutNanos,
      Supplier<Map<String, List<String>>> headerSupplier,
      @Nullable ProxyOptions proxyOptions,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager,
      @Nullable ExecutorService executorService) {
    int callTimeoutMillis =
        (int) Math.min(Duration.ofNanos(timeoutNanos).toMillis(), Integer.MAX_VALUE);
    int connectTimeoutMillis =
        (int) Math.min(Duration.ofNanos(connectionTimeoutNanos).toMillis(), Integer.MAX_VALUE);

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

    boolean isPlainHttp = endpoint.startsWith("http://");
    if (isPlainHttp) {
      builder.connectionSpecs(Collections.singletonList(ConnectionSpec.CLEARTEXT));
    } else if (sslContext != null && trustManager != null) {
      builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
    }

    this.client = builder.build();
    this.url = HttpUrl.get(endpoint);
    this.compressor = compressor;
    this.exportAsJson = exportAsJson;
    this.mediaType = MediaType.parse(contentType);
    this.headerSupplier = headerSupplier;
  }

  @Override
  public void send(
      Marshaler marshaler,
      int contentLength,
      Consumer<Response> onResponse,
      Consumer<Throwable> onError) {
    Request.Builder requestBuilder = new Request.Builder().url(url);

    Map<String, List<String>> headers = headerSupplier.get();
    if (headers != null) {
      headers.forEach(
          (key, values) -> values.forEach(value -> requestBuilder.addHeader(key, value)));
    }
    RequestBody body = new RawRequestBody(marshaler, exportAsJson, contentLength, mediaType);
    if (compressor != null) {
      requestBuilder.addHeader("Content-Encoding", compressor.getEncoding());
      requestBuilder.post(new CompressedRequestBody(compressor, body));
    } else {
      requestBuilder.post(body);
    }

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
                      public void onResponse(Call call, okhttp3.Response response) {
                        try (ResponseBody body = response.body()) {
                          onResponse.accept(
                              new Response() {
                                @Nullable private byte[] bodyBytes;

                                @Override
                                public int statusCode() {
                                  return response.code();
                                }

                                @Override
                                public String statusMessage() {
                                  return response.message();
                                }

                                @Override
                                public byte[] responseBody() throws IOException {
                                  if (bodyBytes == null) {
                                    bodyBytes = body.bytes();
                                  }
                                  return bodyBytes;
                                }
                              });
                        }
                      }
                    }));
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

  static boolean isRetryable(okhttp3.Response response) {
    return RetryUtil.retryableHttpResponseCodes().contains(response.code());
  }

  private static class RawRequestBody extends RequestBody {

    private final Marshaler marshaler;
    private final boolean exportAsJson;
    private final int contentLength;
    private final MediaType mediaType;

    private RawRequestBody(
        Marshaler marshaler, boolean exportAsJson, int contentLength, MediaType mediaType) {
      this.marshaler = marshaler;
      this.exportAsJson = exportAsJson;
      this.contentLength = contentLength;
      this.mediaType = mediaType;
    }

    @Override
    public long contentLength() {
      return contentLength;
    }

    @Override
    public MediaType contentType() {
      return mediaType;
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
      if (exportAsJson) {
        marshaler.writeJsonTo(bufferedSink.outputStream());
      } else {
        marshaler.writeBinaryTo(bufferedSink.outputStream());
      }
    }
  }

  private static class CompressedRequestBody extends RequestBody {
    private final Compressor compressor;
    private final RequestBody requestBody;

    private CompressedRequestBody(Compressor compressor, RequestBody requestBody) {
      this.compressor = compressor;
      this.requestBody = requestBody;
    }

    @Override
    public MediaType contentType() {
      return requestBody.contentType();
    }

    @Override
    public long contentLength() {
      return -1;
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
      BufferedSink compressedSink =
          Okio.buffer(Okio.sink(compressor.compress(bufferedSink.outputStream())));
      requestBody.writeTo(compressedSink);
      compressedSink.close();
    }
  }
}
