/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.metric;

import com.google.rpc.Code;
import com.google.rpc.Status;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.exporter.otlp.internal.MetricAdapter;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
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

/** Exports metrics using OTLP via HTTP, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpHttpMetricExporter implements MetricExporter {

  private static final String EXPORTER_NAME = OtlpHttpMetricExporter.class.getSimpleName();
  private static final Labels EXPORTER_NAME_LABELS = Labels.of("exporter", EXPORTER_NAME);
  private static final Labels EXPORT_SUCCESS_LABELS =
      Labels.of("exporter", EXPORTER_NAME, "success", "true");
  private static final Labels EXPORT_FAILURE_LABELS =
      Labels.of("exporter", EXPORTER_NAME, "success", "false");

  private static final MediaType PROTOBUF_MEDIA_TYPE = MediaType.parse("application/x-protobuf");

  private final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(OtlpHttpMetricExporter.class.getName()));

  private final BoundLongCounter metricsSeen;
  private final BoundLongCounter metricsExportedSuccess;
  private final BoundLongCounter metricsExportedFailure;

  private final OkHttpClient client;
  private final String endpoint;
  @Nullable private final Headers headers;
  private final boolean isCompressionEnabled;

  OtlpHttpMetricExporter(
      OkHttpClient client,
      String endpoint,
      @Nullable Headers headers,
      boolean isCompressionEnabled) {
    Meter meter = GlobalMeterProvider.getMeter("io.opentelemetry.exporters.otlp-http");
    this.metricsSeen =
        meter.longCounterBuilder("metricsSeenByExporter").build().bind(EXPORTER_NAME_LABELS);
    LongCounter metricsExportedCounter =
        meter.longCounterBuilder("metricsExportedByExporter").build();
    this.metricsExportedSuccess = metricsExportedCounter.bind(EXPORT_SUCCESS_LABELS);
    this.metricsExportedFailure = metricsExportedCounter.bind(EXPORT_FAILURE_LABELS);

    this.client = client;
    this.endpoint = endpoint;
    this.headers = headers;
    this.isCompressionEnabled = isCompressionEnabled;
  }

  /**
   * Submits all the given metrics in a single batch to the OpenTelemetry collector.
   *
   * @param metrics the list of Metrics to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    metricsSeen.add(metrics.size());
    ExportMetricsServiceRequest exportMetricsServiceRequest =
        ExportMetricsServiceRequest.newBuilder()
            .addAllResourceMetrics(MetricAdapter.toProtoResourceMetrics(metrics))
            .build();

    Request.Builder requestBuilder = new Request.Builder().url(endpoint);
    if (headers != null) {
      requestBuilder.headers(headers);
    }
    RequestBody requestBody =
        RequestBody.create(exportMetricsServiceRequest.toByteArray(), PROTOBUF_MEDIA_TYPE);
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
                metricsExportedFailure.add(metrics.size());
                result.fail();
                logger.log(
                    Level.SEVERE,
                    "Failed to export metrics. The request could not be executed. Full error message: "
                        + e.getMessage());
              }

              @Override
              public void onResponse(Call call, Response response) {
                if (response.isSuccessful()) {
                  metricsExportedSuccess.add(metrics.size());
                  result.succeed();
                  return;
                }

                metricsExportedFailure.add(metrics.size());
                int code = response.code();

                Status status = extractErrorStatus(response);

                logger.log(
                    Level.WARNING,
                    "Failed to export metrics. Server responded with HTTP status code "
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
   * The OTLP exporter does not batch metrics, so this method will immediately return with success.
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
  public static OtlpHttpMetricExporterBuilder builder() {
    return new OtlpHttpMetricExporterBuilder();
  }

  /**
   * Returns a new {@link OtlpHttpMetricExporter} using the default values.
   *
   * @return a new {@link OtlpHttpMetricExporter} instance.
   */
  public static OtlpHttpMetricExporter getDefault() {
    return builder().build();
  }

  /** Shutdown the exporter. */
  @Override
  public CompletableResultCode shutdown() {
    final CompletableResultCode result = CompletableResultCode.ofSuccess();
    client.dispatcher().cancelAll();
    this.metricsSeen.unbind();
    this.metricsExportedSuccess.unbind();
    this.metricsExportedFailure.unbind();
    return result;
  }
}
