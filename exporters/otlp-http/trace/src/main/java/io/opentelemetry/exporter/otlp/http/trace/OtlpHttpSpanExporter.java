/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import static io.opentelemetry.exporter.otlp.internal.http.OkHttpUtil.gzipRequestBody;
import static io.opentelemetry.exporter.otlp.internal.http.OkHttpUtil.toListenableFuture;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.internal.ProtoRequestBody;
import io.opentelemetry.exporter.otlp.internal.http.HttpStatusException;
import io.opentelemetry.exporter.otlp.internal.retry.RetryExecutor;
import io.opentelemetry.exporter.otlp.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.otlp.internal.traces.TraceRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/** Exports spans using OTLP via HTTP, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpHttpSpanExporter implements SpanExporter {

  private static final String EXPORTER_NAME = OtlpHttpSpanExporter.class.getSimpleName();
  private static final Attributes EXPORTER_NAME_LABELS =
      Attributes.builder().put("exporter", EXPORTER_NAME).build();
  private static final Attributes EXPORT_SUCCESS_LABELS =
      Attributes.builder().put("exporter", EXPORTER_NAME).put("success", true).build();
  private static final Attributes EXPORT_FAILURE_LABELS =
      Attributes.builder().put("exporter", EXPORTER_NAME).put("success", false).build();

  private static final Logger internalLogger =
      Logger.getLogger(OtlpHttpSpanExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final BoundLongCounter spansSeen;
  private final BoundLongCounter spansExportedSuccess;
  private final BoundLongCounter spansExportedFailure;

  private final OkHttpClient client;
  private final String endpoint;
  @Nullable private final Headers headers;
  private final boolean compressionEnabled;
  private final RetryExecutor retryExecutor;

  OtlpHttpSpanExporter(
      OkHttpClient client,
      String endpoint,
      Headers headers,
      boolean compressionEnabled,
      RetryPolicy retryPolicy) {
    Meter meter = GlobalMeterProvider.get().get("io.opentelemetry.exporters.otlp-http");
    this.spansSeen = meter.counterBuilder("spansSeenByExporter").build().bind(EXPORTER_NAME_LABELS);
    LongCounter spansExportedCounter = meter.counterBuilder("spansExportedByExporter").build();
    this.spansExportedSuccess = spansExportedCounter.bind(EXPORT_SUCCESS_LABELS);
    this.spansExportedFailure = spansExportedCounter.bind(EXPORT_FAILURE_LABELS);

    this.client = client;
    this.endpoint = endpoint;
    this.headers = headers;
    this.compressionEnabled = compressionEnabled;
    this.retryExecutor =
        new RetryExecutor(
            OtlpHttpSpanExporter.class.getSimpleName(),
            retryPolicy,
            t -> !(t instanceof HttpStatusException));
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

    TraceRequestMarshaler exportRequest = TraceRequestMarshaler.create(spans);

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

    Futures.addCallback(
        retryExecutor.submit(
            retryContext -> {
              int attemptCount = retryContext.getAttemptCount();
              if (attemptCount > 0) {
                Throwable lastAttemptFailure = retryContext.getLastAttemptFailure();
                String message =
                    lastAttemptFailure == null ? "No error" : lastAttemptFailure.getMessage();
                logger.log(
                    Level.WARNING,
                    "Retrying span export (try "
                        + (attemptCount + 1)
                        + "). Last attempt error message: "
                        + message);
              }
              return toListenableFuture(client.newCall(requestBuilder.build()));
            }),
        new FutureCallback<Response>() {
          @Override
          public void onSuccess(@Nullable Response response) {
            spansExportedSuccess.add(spans.size());
            result.succeed();
          }

          @Override
          public void onFailure(Throwable t) {
            spansExportedFailure.add(spans.size());
            logger.log(Level.WARNING, "Failed to export spans. " + t.getMessage());
            result.fail();
          }
        },
        MoreExecutors.directExecutor());

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
    final CompletableResultCode result = CompletableResultCode.ofSuccess();
    client.dispatcher().cancelAll();
    this.spansSeen.unbind();
    this.spansExportedSuccess.unbind();
    this.spansExportedFailure.unbind();
    return result;
  }
}
