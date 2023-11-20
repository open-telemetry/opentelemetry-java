/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.internal.InstrumentationUtil;
import io.opentelemetry.exporter.internal.RetryUtil;
import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.internal.http.HttpSender;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.IOException;
import java.io.OutputStream;
import java.time.Duration;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/**
 * {@link HttpSender} which is backed by OkHttp.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OkHttpHttpSender implements HttpSender {

  private final OkHttpClient client;
  private final HttpUrl url;
  private final boolean compressionEnabled;
  private final Supplier<Map<String, String>> headerSupplier;
  private final MediaType mediaType;

  /** Create a sender. */
  public OkHttpHttpSender(
      String endpoint,
      boolean compressionEnabled,
      String contentType,
      long timeoutNanos,
      Supplier<Map<String, String>> headerSupplier,
      @Nullable Authenticator authenticator,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager) {
    OkHttpClient.Builder builder =
        new OkHttpClient.Builder()
            .dispatcher(OkHttpUtil.newDispatcher())
            .callTimeout(Duration.ofNanos(timeoutNanos));

    if (authenticator != null) {
      Authenticator finalAuthenticator = authenticator;
      // Generate and attach OkHttp Authenticator implementation
      builder.authenticator(
          (route, response) -> {
            Request.Builder requestBuilder = response.request().newBuilder();
            finalAuthenticator.getHeaders().forEach(requestBuilder::header);
            return requestBuilder.build();
          });
    }

    if (retryPolicy != null) {
      builder.addInterceptor(new RetryInterceptor(retryPolicy, OkHttpHttpSender::isRetryable));
    }
    if (sslContext != null && trustManager != null) {
      builder.sslSocketFactory(sslContext.getSocketFactory(), trustManager);
    }
    this.client = builder.build();
    this.url = HttpUrl.get(endpoint);
    this.compressionEnabled = compressionEnabled;
    this.mediaType = MediaType.parse(contentType);
    this.headerSupplier = headerSupplier;
  }

  @Override
  public void send(
      Consumer<OutputStream> marshaler,
      int contentLength,
      Consumer<Response> onResponse,
      Consumer<Throwable> onError) {
    Request.Builder requestBuilder = new Request.Builder().url(url);

    Map<String, String> headers = headerSupplier.get();
    if (headers != null) {
      headers.forEach(requestBuilder::addHeader);
    }
    RequestBody body = new RawRequestBody(marshaler, contentLength, mediaType);
    if (compressionEnabled) {
      requestBuilder.addHeader("Content-Encoding", "gzip");
      requestBuilder.post(new GzipRequestBody(body));
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
                                  return body.bytes();
                                }
                              });
                        }
                      }
                    }));
  }

  @Override
  public CompletableResultCode shutdown() {
    client.dispatcher().cancelAll();
    client.dispatcher().executorService().shutdownNow();
    client.connectionPool().evictAll();
    return CompletableResultCode.ofSuccess();
  }

  static boolean isRetryable(okhttp3.Response response) {
    return RetryUtil.retryableHttpResponseCodes().contains(response.code());
  }

  private static class RawRequestBody extends RequestBody {

    private final Consumer<OutputStream> marshaler;
    private final int contentLength;
    private final MediaType mediaType;

    private RawRequestBody(
        Consumer<OutputStream> marshaler, int contentLength, MediaType mediaType) {
      this.marshaler = marshaler;
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
    public void writeTo(BufferedSink bufferedSink) {
      marshaler.accept(bufferedSink.outputStream());
    }
  }

  private static class GzipRequestBody extends RequestBody {
    private final RequestBody requestBody;

    private GzipRequestBody(RequestBody requestBody) {
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
      BufferedSink gzipSink = Okio.buffer(new GzipSink(bufferedSink));
      requestBody.writeTo(gzipSink);
      gzipSink.close();
    }
  }
}
