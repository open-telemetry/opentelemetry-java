/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.okhttp;

import io.opentelemetry.exporter.otlp.internal.ExporterMetrics;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.grpc.GrpcStatusUtil;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.GzipSink;
import okio.Okio;

/** An exporter for http/protobuf using a signal-specific Marshaler. */
@SuppressWarnings("checkstyle:JavadocMethod")
public final class OkHttpExporter<T extends Marshaler> {

  private static final Logger internalLogger = Logger.getLogger(OkHttpExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final String type;
  private final OkHttpClient client;
  private final String endpoint;
  @Nullable private final Headers headers;
  private final boolean compressionEnabled;

  private final ExporterMetrics exporterMetrics;

  OkHttpExporter(
      String type,
      OkHttpClient client,
      String endpoint,
      @Nullable Headers headers,
      boolean compressionEnabled) {
    this.type = type;
    this.client = client;
    this.endpoint = endpoint;
    this.headers = headers;
    this.compressionEnabled = compressionEnabled;

    this.exporterMetrics = ExporterMetrics.createHttpProtobuf(type);
  }

  public CompletableResultCode export(T exportRequest, int numItems) {
    exporterMetrics.addSeen(numItems);

    Request.Builder requestBuilder = new Request.Builder().url(endpoint);
    if (headers != null) {
      requestBuilder.headers(headers);
    }
    RequestBody requestBody = new ProtoRequestBody(exportRequest);
    if (compressionEnabled) {
      requestBuilder.addHeader("Content-Encoding", "gzip");
      requestBuilder.post(gzipRequestBody(requestBody));
    } else {
      requestBuilder.post(requestBody);
    }

    CompletableResultCode result = new CompletableResultCode();

    client
        .newCall(requestBuilder.build())
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(Call call, IOException e) {
                exporterMetrics.addFailed(numItems);
                logger.log(
                    Level.SEVERE,
                    "Failed to export "
                        + type
                        + "s. The request could not be executed. Full error message: "
                        + e.getMessage());
                result.fail();
              }

              @Override
              public void onResponse(Call call, Response response) {
                try (ResponseBody body = response.body()) {
                  if (response.isSuccessful()) {
                    exporterMetrics.addSuccess(numItems);
                    result.succeed();
                    return;
                  }

                  exporterMetrics.addFailed(numItems);
                  int code = response.code();

                  String status = extractErrorStatus(response, body);

                  logger.log(
                      Level.WARNING,
                      "Failed to export "
                          + type
                          + "s. Server responded with HTTP status code "
                          + code
                          + ". Error message: "
                          + status);
                  result.fail();
                }
              }
            });

    return result;
  }

  public CompletableResultCode shutdown() {
    final CompletableResultCode result = CompletableResultCode.ofSuccess();
    client.dispatcher().cancelAll();
    client.dispatcher().executorService().shutdownNow();
    client.connectionPool().evictAll();
    exporterMetrics.unbind();
    return result;
  }

  private static RequestBody gzipRequestBody(RequestBody requestBody) {
    return new RequestBody() {
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
    };
  }

  private static String extractErrorStatus(Response response, @Nullable ResponseBody responseBody) {
    if (responseBody == null) {
      return "Response body missing, HTTP status message: " + response.message();
    }
    try {
      return GrpcStatusUtil.getStatusMessage(responseBody.bytes());
    } catch (IOException e) {
      return "Unable to parse response body, HTTP status message: " + response.message();
    }
  }
}
