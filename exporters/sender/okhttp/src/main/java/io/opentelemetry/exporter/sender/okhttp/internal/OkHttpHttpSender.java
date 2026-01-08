/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.api.internal.InstrumentationUtil;
import io.opentelemetry.exporter.compressor.Compressor;
import io.opentelemetry.exporter.http.HttpRequestBodyWriter;
import io.opentelemetry.exporter.http.HttpResponse;
import io.opentelemetry.exporter.http.HttpSender;
import io.opentelemetry.exporter.internal.RetryUtil;
import io.opentelemetry.sdk.common.CompletableResultCode;
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

  private static final Logger logger = Logger.getLogger(OkHttpHttpSender.class.getName());

  private final boolean managedExecutor;
  private final OkHttpClient client;
  private final HttpUrl url;
  private final Supplier<Map<String, List<String>>> headerSupplier;
  private final MediaType mediaType;
  @Nullable private final Compressor compressor;

  /** Create a sender. */
  @SuppressWarnings("TooManyParameters")
  public OkHttpHttpSender(
      URI endpoint,
      String contentType,
      @Nullable Compressor compressor,
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
  }

  @Override
  public void send(
      HttpRequestBodyWriter requestBodyWriter,
      Consumer<HttpResponse> onResponse,
      Consumer<Throwable> onError) {
    Request.Builder requestBuilder = new Request.Builder().url(url);

    Map<String, List<String>> headers = headerSupplier.get();
    if (headers != null) {
      headers.forEach(
          (key, values) -> values.forEach(value -> requestBuilder.addHeader(key, value)));
    }
    if (compressor != null) {
      requestBuilder.addHeader("Content-Encoding", compressor.getEncoding());
    }
    requestBuilder.post(new RequestBodyImpl(requestBodyWriter, compressor, mediaType));

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
                              new HttpResponse() {
                                @Nullable private byte[] bodyBytes;

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
                                  if (bodyBytes == null) {
                                    try {
                                      bodyBytes = body.bytes();
                                    } catch (IOException e) {
                                      bodyBytes = new byte[0];
                                      logger.log(Level.WARNING, "Failed to read response body", e);
                                    }
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

  private static class RequestBodyImpl extends RequestBody {

    private final HttpRequestBodyWriter requestBodyWriter;
    @Nullable private final Compressor compressor;
    private final MediaType mediaType;

    private RequestBodyImpl(
        HttpRequestBodyWriter requestBodyWriter,
        @Nullable Compressor compressor,
        MediaType mediaType) {
      this.requestBodyWriter = requestBodyWriter;
      this.compressor = compressor;
      this.mediaType = mediaType;
    }

    @Override
    public long contentLength() {
      return compressor == null ? requestBodyWriter.contentLength() : -1;
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
        requestBodyWriter.writeRequestBody(compressedSink.outputStream());
        compressedSink.close();
      } else {
        requestBodyWriter.writeRequestBody(bufferedSink.outputStream());
      }
    }
  }
}
