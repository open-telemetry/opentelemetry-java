/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.exporter.otlp.internal.SpanAdapter;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.io.IOException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
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

/** Exports spans using OTLP via HTTP, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpHttpSpanExporter implements SpanExporter {

  private static final String EXPORTER_NAME = OtlpHttpSpanExporter.class.getSimpleName();
  private static final Labels EXPORTER_NAME_LABELS = Labels.of("exporter", EXPORTER_NAME);
  private static final Labels EXPORT_SUCCESS_LABELS =
      Labels.of("exporter", EXPORTER_NAME, "success", "true");
  private static final Labels EXPORT_FAILURE_LABELS =
      Labels.of("exporter", EXPORTER_NAME, "success", "false");

  private static final MediaType PROTOBUF_MEDIA_TYPE = MediaType.parse("application/x-protobuf");

  private final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(OtlpHttpSpanExporter.class.getName()));

  private final BoundLongCounter spansSeen;
  private final BoundLongCounter spansExportedSuccess;
  private final BoundLongCounter spansExportedFailure;

  private final OkHttpClient client;
  private final String endpoint;
  private final Headers headers;
  private final boolean isCompressionEnabled;

  OtlpHttpSpanExporter(
      OkHttpClient client, String endpoint, Headers headers, boolean isCompressionEnabled) {
    Meter meter = GlobalMeterProvider.getMeter("io.opentelemetry.exporters.otlp-http");
    this.spansSeen =
        meter.longCounterBuilder("spansSeenByExporter").build().bind(EXPORTER_NAME_LABELS);
    LongCounter spansExportedCounter = meter.longCounterBuilder("spansExportedByExporter").build();
    this.spansExportedSuccess = spansExportedCounter.bind(EXPORT_SUCCESS_LABELS);
    this.spansExportedFailure = spansExportedCounter.bind(EXPORT_FAILURE_LABELS);

    this.client = client;
    this.endpoint = endpoint;
    this.headers = headers;
    this.isCompressionEnabled = isCompressionEnabled;
  }

  /**
   * Submits all the given spans in a single batch to the OpenTelemetry collector.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    spansSeen.add(spans.size());
    ExportTraceServiceRequest exportTraceServiceRequest =
        ExportTraceServiceRequest.newBuilder()
            .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(spans))
            .build();

    Request.Builder requestBuilder = new Request.Builder().url(endpoint);
    if (headers != null) {
      requestBuilder.headers(headers);
    }
    RequestBody requestBody =
        RequestBody.create(exportTraceServiceRequest.toByteArray(), PROTOBUF_MEDIA_TYPE);
    if (isCompressionEnabled) {
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
                spansExportedFailure.add(spans.size());
                result.fail();
                logger.log(
                    Level.SEVERE,
                    "Failed to export spans. The request could not be executed. Full error message: "
                        + e.getMessage());
              }

              @Override
              public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                  spansExportedSuccess.add(spans.size());
                  result.succeed();
                  return;
                }

                spansExportedFailure.add(spans.size());
                int code = response.code();

                Status status = extractErrorStatus(response);

                logger.log(
                    Level.WARNING,
                    "Failed to export spans. Server responded with HTTP status code "
                        + code
                        + ". Error message: "
                        + status.getMessage());
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

  private static Status extractErrorStatus(Response response) {
    ResponseBody responseBody = response.body();
    if (responseBody == null) {
      return Status.newBuilder()
          .setMessage("Response body missing, HTTP status message: " + response.message())
          .setCode(Code.UNKNOWN.getNumber())
          .build();
    }
    try {
      return Status.parseFrom(responseBody.bytes());
    } catch (IOException e) {
      return Status.newBuilder()
          .setMessage("Unable to parse response body, HTTP status message: " + response.message())
          .setCode(Code.UNKNOWN.getNumber())
          .build();
    }
  }

  /**
   * The OTLP exporter does not batch spans, so this method will immediately return with success.
   *
   * @return always Success
   */
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpHttpSpanExporterBuilder builder() {
    return new OtlpHttpSpanExporterBuilder();
  }

  /**
   * Returns a new {@link OtlpHttpSpanExporter} using the default values.
   *
   * @return a new {@link OtlpHttpSpanExporter} instance.
   */
  public static OtlpHttpSpanExporter getDefault() {
    return builder().build();
  }

  /** Shutdown the exporter. */
  @Override
  public CompletableResultCode shutdown() {
    final CompletableResultCode result = CompletableResultCode.ofSuccess();
    client.dispatcher().cancelAll();
    this.spansSeen.unbind();
    this.spansExportedSuccess.unbind();
    this.spansExportedFailure.unbind();
    return result;
  }
}
