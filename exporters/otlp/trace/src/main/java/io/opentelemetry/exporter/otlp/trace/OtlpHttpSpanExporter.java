/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

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
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

/** Exports spans using OTLP via HTTP, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpHttpSpanExporter implements SpanExporter {

  private static final String EXPORTER_NAME = OtlpHttpSpanExporter.class.getSimpleName();
  private static final Labels EXPORTER_NAME_LABELS = Labels.of("exporter", EXPORTER_NAME);
  private static final Labels EXPORT_SUCCESS_LABELS =
      Labels.of("exporter", EXPORTER_NAME, "success", "true");
  private static final Labels EXPORT_FAILURE_LABELS =
      Labels.of("exporter", EXPORTER_NAME, "success", "false");

  private final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(OtlpHttpSpanExporter.class.getName()));

  private final BoundLongCounter spansSeen;
  private final BoundLongCounter spansExportedSuccess;
  private final BoundLongCounter spansExportedFailure;

  private final OkHttpClient client;
  private final String endpoint;
  private final Map<String, List<String>> headers;
  private final RequestResponseHandler requestResponseHandler;

  OtlpHttpSpanExporter(
      OkHttpClient client,
      String endpoint,
      Map<String, List<String>> headers,
      RequestResponseHandler requestResponseHandler) {
    // TODO: should this be io.opentelemetry.exporters.otlp with a label that indicates the
    // protocol?
    Meter meter = GlobalMeterProvider.getMeter("io.opentelemetry.exporters.otlp-http");
    this.spansSeen =
        meter.longCounterBuilder("spansSeenByExporter").build().bind(EXPORTER_NAME_LABELS);
    LongCounter spansExportedCounter = meter.longCounterBuilder("spansExportedByExporter").build();
    this.spansExportedSuccess = spansExportedCounter.bind(EXPORT_SUCCESS_LABELS);
    this.spansExportedFailure = spansExportedCounter.bind(EXPORT_FAILURE_LABELS);

    this.client = client;
    this.endpoint = endpoint;
    this.headers = headers;
    this.requestResponseHandler = requestResponseHandler;
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

    Request.Builder requestBuilder =
        new Request.Builder()
            .url(endpoint)
            .post(requestResponseHandler.build(exportTraceServiceRequest));
    if (headers != null) {
      headers.forEach(
          (key, values) -> values.forEach(value -> requestBuilder.addHeader(key, value)));
    }

    CompletableResultCode result = new CompletableResultCode();

    client
        .newCall(requestBuilder.build())
        .enqueue(
            new Callback() {
              @Override
              public void onFailure(@NotNull Call call, @NotNull IOException e) {
                spansExportedFailure.add(spans.size());
                result.fail();
                logger.log(
                    Level.SEVERE,
                    "Failed to export spans. The request could not be executed. Full error message: "
                        + e.getMessage());
              }

              @Override
              public void onResponse(@NotNull Call call, @NotNull Response response) {
                if (response.isSuccessful()) {
                  spansExportedSuccess.add(spans.size());
                  result.succeed();
                  return;
                }

                spansExportedFailure.add(spans.size());
                result.fail();
                int code = response.code();

                Status status;
                try {
                  status = requestResponseHandler.extractErrorStatus(response.body());
                } catch (IOException e) {
                  status =
                      Status.newBuilder()
                          .setMessage(
                              "Unable to extract error message from request: " + e.getMessage())
                          .build();
                }

                logger.log(
                    Level.SEVERE,
                    "Failed to export spans. Server responded with code "
                        + code
                        + ". Error message: "
                        + status.getMessage());
              }
            });

    return result;
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
    // TODO: should we update a boolean shutdown flag which prevents future exports?
    final CompletableResultCode result = CompletableResultCode.ofSuccess();
    this.spansSeen.unbind();
    this.spansExportedSuccess.unbind();
    this.spansExportedFailure.unbind();
    return result;
  }

  interface RequestResponseHandler {

    /**
     * Build a request body for the export trace service request.
     *
     * @param exportTraceServiceRequest the export trace service request
     * @return the request body
     */
    RequestBody build(ExportTraceServiceRequest exportTraceServiceRequest);

    /**
     * Extract the status from the response body. Called when response status code is 4XX and 5XX.
     *
     * @param responseBody the response body
     * @return the status
     * @throws IOException if unable to extract a status from the response body
     */
    Status extractErrorStatus(ResponseBody responseBody) throws IOException;
  }
}
