/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterMetrics;
import io.opentelemetry.exporter.internal.ExporterMetricsAdapter;
import io.opentelemetry.exporter.internal.FailedExportException;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterUtil;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.HealthMetricLevel;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * An exporter for http/protobuf or http/json using a signal-specific Marshaler.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("checkstyle:JavadocMethod")
public final class HttpExporter<T extends Marshaler> {

  private static final Logger internalLogger = Logger.getLogger(HttpExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);
  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final String type;
  private final HttpSender httpSender;
  private final ExporterMetricsAdapter exporterMetrics;

  public HttpExporter(
      String legacyExporterName,
      ExporterMetrics.Signal type,
      ComponentId componentId,
      HttpSender httpSender,
      Supplier<MeterProvider> meterProviderSupplier,
      HealthMetricLevel healthMetricLevel,
      boolean exportAsJson,
      Attributes healthMetricAttributes) {
    this.type = type.toString();
    this.httpSender = httpSender;
    // TODO: extract server.address and server.port here
    this.exporterMetrics =
        new ExporterMetricsAdapter(
            healthMetricLevel,
            meterProviderSupplier,
            type,
            componentId,
            healthMetricAttributes,
            legacyExporterName,
            exportAsJson ? "http-json" : "http");
  }

  public CompletableResultCode export(T exportRequest, int numItems) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    ExporterMetricsAdapter.Recording metricRecording =
        exporterMetrics.startRecordingExport(numItems);

    CompletableResultCode result = new CompletableResultCode();

    httpSender.send(
        exportRequest,
        exportRequest.getBinarySerializedSize(),
        httpResponse -> onResponse(result, metricRecording, httpResponse),
        throwable -> onError(result, metricRecording, throwable));

    return result;
  }

  private void onResponse(
      CompletableResultCode result,
      ExporterMetricsAdapter.Recording metricRecording,
      HttpSender.Response httpResponse) {
    int statusCode = httpResponse.statusCode();

    if (statusCode >= 200 && statusCode < 300) {
      metricRecording.finishSuccessful();
      result.succeed();
      return;
    }

    metricRecording.finishFailed("" + statusCode);

    byte[] body = null;
    try {
      body = httpResponse.responseBody();
    } catch (IOException ex) {
      logger.log(Level.FINE, "Unable to obtain response body", ex);
    }

    String status = extractErrorStatus(httpResponse.statusMessage(), body);

    logger.log(
        Level.WARNING,
        "Failed to export "
            + type
            + "s. Server responded with HTTP status code "
            + statusCode
            + ". Error message: "
            + status);

    result.failExceptionally(FailedExportException.httpFailedWithResponse(httpResponse));
  }

  private void onError(
      CompletableResultCode result, ExporterMetricsAdapter.Recording metricRecording, Throwable e) {
    metricRecording.finishFailed(e);
    logger.log(
        Level.SEVERE,
        "Failed to export "
            + type
            + "s. The request could not be executed. Full error message: "
            + e.getMessage(),
        e);
    result.failExceptionally(FailedExportException.httpFailedExceptionally(e));
  }

  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return httpSender.shutdown();
  }

  private static String extractErrorStatus(String statusMessage, @Nullable byte[] responseBody) {
    if (responseBody == null) {
      return "Response body missing, HTTP status message: " + statusMessage;
    }
    try {
      return GrpcExporterUtil.getStatusMessage(responseBody);
    } catch (IOException e) {
      return "Unable to parse response body, HTTP status message: " + statusMessage;
    }
  }
}
