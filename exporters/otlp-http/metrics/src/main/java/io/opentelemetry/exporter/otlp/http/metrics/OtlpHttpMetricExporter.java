/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.metrics;

import static io.opentelemetry.exporter.otlp.internal.http.OkHttpUtil.gzipRequestBody;
import static io.opentelemetry.exporter.otlp.internal.http.OkHttpUtil.toListenableFuture;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.opentelemetry.exporter.otlp.internal.ProtoRequestBody;
import io.opentelemetry.exporter.otlp.internal.http.HttpStatusException;
import io.opentelemetry.exporter.otlp.internal.metrics.MetricsRequestMarshaler;
import io.opentelemetry.exporter.otlp.internal.retry.RetryExecutor;
import io.opentelemetry.exporter.otlp.internal.retry.RetryPolicy;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
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

/** Exports metrics using OTLP via HTTP, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpHttpMetricExporter implements MetricExporter {

  private static final Logger internalLogger =
      Logger.getLogger(OtlpHttpMetricExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final OkHttpClient client;
  private final String endpoint;
  @Nullable private final Headers headers;
  private final boolean compressionEnabled;
  private final RetryExecutor retryExecutor;

  OtlpHttpMetricExporter(
      OkHttpClient client,
      String endpoint,
      @Nullable Headers headers,
      boolean compressionEnabled,
      RetryPolicy retryPolicy) {
    this.client = client;
    this.endpoint = endpoint;
    this.headers = headers;
    this.compressionEnabled = compressionEnabled;
    this.retryExecutor =
        new RetryExecutor(
            OtlpHttpMetricExporter.class.getSimpleName(),
            retryPolicy,
            t -> !(t instanceof HttpStatusException));
  }

  /**
   * Submits all the given metrics in a single batch to the OpenTelemetry collector.
   *
   * @param metrics the list of Metrics to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    MetricsRequestMarshaler exportRequest = MetricsRequestMarshaler.create(metrics);

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
                    "Retrying metric export (try "
                        + (attemptCount + 1)
                        + "). Last attempt error message: "
                        + message);
              }
              return toListenableFuture(client.newCall(requestBuilder.build()));
            }),
        new FutureCallback<Response>() {
          @Override
          public void onSuccess(@Nullable Response response) {
            result.succeed();
          }

          @Override
          public void onFailure(Throwable t) {
            logger.log(Level.WARNING, "Failed to export metrics. " + t.getMessage());
            result.fail();
          }
        },
        MoreExecutors.directExecutor());

    return result;
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
    return result;
  }
}
