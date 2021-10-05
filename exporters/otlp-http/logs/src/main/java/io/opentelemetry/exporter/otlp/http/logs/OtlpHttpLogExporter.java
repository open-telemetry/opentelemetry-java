/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.internal.ProtoRequestBody;
import io.opentelemetry.exporter.otlp.internal.grpc.GrpcStatusUtil;
import io.opentelemetry.exporter.otlp.internal.logs.LogsRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.logging.export.LogExporter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
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

/** Exports logs using OTLP via HTTP, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpHttpLogExporter implements LogExporter {

  private static final String EXPORTER_NAME = OtlpHttpLogExporter.class.getSimpleName();
  private static final Attributes EXPORTER_NAME_LABELS =
      Attributes.builder().put("exporter", EXPORTER_NAME).build();
  private static final Attributes EXPORT_SUCCESS_LABELS =
      Attributes.builder().put("exporter", EXPORTER_NAME).put("success", true).build();
  private static final Attributes EXPORT_FAILURE_LABELS =
      Attributes.builder().put("exporter", EXPORTER_NAME).put("success", false).build();

  private static final Logger internalLogger =
      Logger.getLogger(OtlpHttpLogExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final BoundLongCounter logsSeen;
  private final BoundLongCounter logsExportedSuccess;
  private final BoundLongCounter logsExportedFailure;

  private final OkHttpClient client;
  private final String endpoint;
  @Nullable private final Headers headers;
  private final boolean compressionEnabled;

  OtlpHttpLogExporter(
      OkHttpClient client, String endpoint, @Nullable Headers headers, boolean compressionEnabled) {
    Meter meter = GlobalMeterProvider.get().get("io.opentelemetry.exporters.otlp-http");
    this.logsSeen = meter.counterBuilder("logsSeenByExporter").build().bind(EXPORTER_NAME_LABELS);
    LongCounter logsExportedCounter = meter.counterBuilder("logsExportedByExporter").build();
    this.logsExportedSuccess = logsExportedCounter.bind(EXPORT_SUCCESS_LABELS);
    this.logsExportedFailure = logsExportedCounter.bind(EXPORT_FAILURE_LABELS);

    this.client = client;
    this.endpoint = endpoint;
    this.headers = headers;
    this.compressionEnabled = compressionEnabled;
  }

  /**
   * Submits all the given logs in a single batch to the OpenTelemetry collector.
   *
   * @param logs the list of sampled Logs to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<LogRecord> logs) {
    logsSeen.add(logs.size());

    LogsRequestMarshaler exportRequest = LogsRequestMarshaler.create(logs);

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
                logsExportedFailure.add(logs.size());
                logger.log(
                    Level.SEVERE,
                    "Failed to export logs. The request could not be executed. Full error message: "
                        + e.getMessage());
                result.fail();
              }

              @Override
              public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                  logsExportedSuccess.add(logs.size());
                  result.succeed();
                  return;
                }

                logsExportedFailure.add(logs.size());
                int code = response.code();

                String status = extractErrorStatus(response);

                logger.log(
                    Level.WARNING,
                    "Failed to export logs. Server responded with HTTP status code "
                        + code
                        + ". Error message: "
                        + status);
                result.fail();
              }
            });

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

  private static String extractErrorStatus(Response response) {
    ResponseBody responseBody = response.body();
    if (responseBody == null) {
      return "Response body missing, HTTP status message: " + response.message();
    }
    try {
      return GrpcStatusUtil.getStatusMessage(responseBody.bytes());
    } catch (IOException e) {
      return "Unable to parse response body, HTTP status message: " + response.message();
    }
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpHttpLogExporterBuilder builder() {
    return new OtlpHttpLogExporterBuilder();
  }

  /**
   * Returns a new {@link OtlpHttpLogExporter} using the default values.
   *
   * @return a new {@link OtlpHttpLogExporter} instance.
   */
  public static OtlpHttpLogExporter getDefault() {
    return builder().build();
  }

  /** Shutdown the exporter. */
  @Override
  public CompletableResultCode shutdown() {
    final CompletableResultCode result = CompletableResultCode.ofSuccess();
    client.dispatcher().cancelAll();
    this.logsSeen.unbind();
    this.logsExportedSuccess.unbind();
    this.logsExportedFailure.unbind();
    return result;
  }
}
