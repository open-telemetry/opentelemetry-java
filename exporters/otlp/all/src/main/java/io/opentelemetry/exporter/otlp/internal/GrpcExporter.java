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
import io.opentelemetry.sdk.common.export.GrpcResponse;
import io.opentelemetry.sdk.common.export.GrpcSender;
import io.opentelemetry.sdk.common.export.GrpcStatusCode;
import io.opentelemetry.sdk.common.export.MessageWriter;
import io.opentelemetry.sdk.common.internal.StandardComponentId;
import io.opentelemetry.sdk.common.internal.ThrottlingLogger;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generic gRPC exporter.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("checkstyle:JavadocMethod")
public final class GrpcExporter {

  private static final Logger internalLogger = Logger.getLogger(GrpcExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  // We only log unimplemented once since it's a configuration issue that won't be recovered.
  private final AtomicBoolean loggedUnimplemented = new AtomicBoolean();
  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final String type;
  private final GrpcSender grpcSender;
  private final ExporterInstrumentation exporterMetrics;
  private final long maxRequestMessageSize;

  public GrpcExporter(
      GrpcSender grpcSender,
      InternalTelemetryVersion internalTelemetryVersion,
      StandardComponentId componentId,
      Supplier<MeterProvider> meterProviderSupplier,
      URI endpoint) {
    this(
        grpcSender,
        internalTelemetryVersion,
        componentId,
        meterProviderSupplier,
        endpoint,
        Long.MAX_VALUE);
  }

  public GrpcExporter(
      GrpcSender grpcSender,
      InternalTelemetryVersion internalTelemetryVersion,
      StandardComponentId componentId,
      Supplier<MeterProvider> meterProviderSupplier,
      URI endpoint,
      long maxRequestMessageSize) {
    this.type = componentId.getStandardType().signal().logFriendlyName();
    this.grpcSender = grpcSender;
    this.exporterMetrics =
        new ExporterInstrumentation(
            internalTelemetryVersion, meterProviderSupplier, componentId, endpoint);
    this.maxRequestMessageSize = maxRequestMessageSize;
  }

  public CompletableResultCode export(Marshaler exportRequest, int numItems) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    ExporterInstrumentation.Recording metricRecording =
        exporterMetrics.startRecordingExport(numItems);

    CompletableResultCode result = new CompletableResultCode();
    MessageWriter messageWriter = exportRequest.toBinaryMessageWriter();

    long requestMessageSize = getRequestMessageSize(messageWriter);
    if (requestMessageSize > maxRequestMessageSize) {
      return failRequestTooLarge(result, metricRecording, requestMessageSize);
    }

    grpcSender.send(
        messageWriter,
        grpcResponse -> onResponse(result, metricRecording, grpcResponse),
        throwable -> onError(result, metricRecording, throwable));

    return result;
  }

  private CompletableResultCode failRequestTooLarge(
      CompletableResultCode result,
      ExporterInstrumentation.Recording metricRecording,
      long requestMessageSize) {
    String errorMessage =
        "OTLP gRPC request message size "
            + requestMessageSize
            + " exceeded limit of "
            + maxRequestMessageSize
            + " bytes";
    IOException exception = new IOException(errorMessage);
    metricRecording.finishFailed(exception);
    logger.log(Level.WARNING, errorMessage);
    result.failExceptionally(FailedExportException.grpcFailedExceptionally(exception));
    return result;
  }

  private static long getRequestMessageSize(MessageWriter messageWriter) {
    int contentLength = messageWriter.getContentLength();
    if (contentLength >= 0) {
      return contentLength;
    }
    try {
      CountingOutputStream countingOutputStream = new CountingOutputStream();
      messageWriter.writeMessage(countingOutputStream);
      return countingOutputStream.getCount();
    } catch (IOException e) {
      return Long.MAX_VALUE;
    }
  }

  private static final class CountingOutputStream extends OutputStream {
    private long count;

    @Override
    public void write(int b) {
      count++;
    }

    @Override
    public void write(byte[] b, int off, int len) {
      count += len;
    }

    private long getCount() {
      return count;
    }
  }

  private void onResponse(
      CompletableResultCode result,
      ExporterInstrumentation.Recording metricRecording,
      GrpcResponse grpcResponse) {
    GrpcStatusCode statusCode = grpcResponse.getStatusCode();

    metricRecording.setGrpcStatusCode(statusCode);

    if (statusCode == GrpcStatusCode.OK) {
      metricRecording.finishSuccessful();
      result.succeed();
      return;
    }

    metricRecording.finishFailed(String.valueOf(statusCode.getValue()));
    switch (statusCode) {
      case UNIMPLEMENTED:
        if (loggedUnimplemented.compareAndSet(false, true)) {
          GrpcExporterUtil.logUnimplemented(
              internalLogger, type, grpcResponse.getStatusDescription());
        }
        break;
      case UNAVAILABLE:
        logger.log(
            Level.SEVERE,
            "Failed to export "
                + type
                + "s. Server is UNAVAILABLE. "
                + "Make sure your collector is running and reachable from this network. "
                + "Full error message:"
                + grpcResponse.getStatusDescription());
        break;
      default:
        logger.log(
            Level.WARNING,
            "Failed to export "
                + type
                + "s. Server responded with gRPC status code "
                + statusCode.getValue()
                + ". Error message: "
                + grpcResponse.getStatusDescription());
        break;
    }
    result.failExceptionally(FailedExportException.grpcFailedWithResponse(grpcResponse));
  }

  private void onError(
      CompletableResultCode result,
      ExporterInstrumentation.Recording metricRecording,
      Throwable e) {
    metricRecording.finishFailed(e);
    logger.log(
        Level.SEVERE, "Failed to export " + type + "s. The request could not be executed.", e);
    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "Failed to export " + type + "s. Details follow:", e);
    }
    result.failExceptionally(FailedExportException.grpcFailedExceptionally(e));
  }

  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return grpcSender.shutdown();
  }
}
