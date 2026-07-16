/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.FailedExportException;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.metrics.ExporterInstrumentation;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.HttpResponse;
import io.opentelemetry.sdk.common.export.HttpSender;
import io.opentelemetry.sdk.common.export.MessageWriter;
import io.opentelemetry.sdk.common.internal.StandardComponentId;
import io.opentelemetry.sdk.common.internal.ThrottlingLogger;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
public final class HttpExporter {
  // Limit logged response body text to avoid flooding warnings with large payloads.
  private static final int MAX_RESPONSE_BODY_LOG_LENGTH = 1024;

  private static final Logger internalLogger = Logger.getLogger(HttpExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);
  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final String type;
  private final HttpSender httpSender;
  private final ExporterInstrumentation exporterMetrics;
  private final boolean exportAsJson;

  public HttpExporter(
      StandardComponentId componentId,
      HttpSender httpSender,
      Supplier<MeterProvider> meterProviderSupplier,
      InternalTelemetryVersion internalTelemetryVersion,
      URI endpoint,
      boolean exportAsJson) {
    this.type = componentId.getStandardType().signal().logFriendlyName();
    this.httpSender = httpSender;
    this.exporterMetrics =
        new ExporterInstrumentation(
            internalTelemetryVersion, meterProviderSupplier, componentId, endpoint);
    this.exportAsJson = exportAsJson;
  }

  public CompletableResultCode export(Marshaler exportRequest, int numItems) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    ExporterInstrumentation.Recording metricRecording =
        exporterMetrics.startRecordingExport(numItems);

    CompletableResultCode result = new CompletableResultCode();
    MessageWriter messageWriter =
        exportAsJson ? exportRequest.toJsonMessageWriter() : exportRequest.toBinaryMessageWriter();

    httpSender.send(
        messageWriter,
        httpResponse -> onResponse(result, metricRecording, httpResponse),
        throwable -> onError(result, metricRecording, throwable));

    return result;
  }

  private void onResponse(
      CompletableResultCode result,
      ExporterInstrumentation.Recording metricRecording,
      HttpResponse httpResponse) {
    int statusCode = httpResponse.getStatusCode();

    metricRecording.setHttpStatusCode(statusCode);

    if (statusCode >= 200 && statusCode < 300) {
      metricRecording.finishSuccessful();
      result.succeed();
      return;
    }

    metricRecording.finishFailed(String.valueOf(statusCode));

    byte[] body = httpResponse.getResponseBody();

    String status = extractErrorStatus(httpResponse.getStatusMessage(), body);

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
      CompletableResultCode result,
      ExporterInstrumentation.Recording metricRecording,
      Throwable e) {
    metricRecording.finishFailed(e);
    logger.log(
        Level.SEVERE, "Failed to export " + type + "s. The request could not be executed.", e);
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
    if (responseBody.length == 0) {
      return "Response body has 0 length, HTTP status message: " + statusMessage;
    }
    try {
      return GrpcExporterUtil.getStatusMessage(responseBody);
    } catch (IOException e) {
      return extractResponseBodyMessage(responseBody, statusMessage);
    }
  }

  private static String extractResponseBodyMessage(byte[] responseBody, String statusMessage) {
    int lengthToRead = Math.min(responseBody.length, MAX_RESPONSE_BODY_LOG_LENGTH);
    String responseBodyText =
        new String(responseBody, 0, lengthToRead, StandardCharsets.UTF_8).trim();
    if (responseBodyText.isEmpty()) {
      return "HTTP status message: " + statusMessage;
    }
    return "Response body: " + responseBodyText + ", HTTP status message: " + statusMessage;
  }
}
