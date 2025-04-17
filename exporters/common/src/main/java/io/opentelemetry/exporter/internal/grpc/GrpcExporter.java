/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static io.opentelemetry.exporter.internal.grpc.GrpcExporterUtil.GRPC_STATUS_UNAVAILABLE;
import static io.opentelemetry.exporter.internal.grpc.GrpcExporterUtil.GRPC_STATUS_UNIMPLEMENTED;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterMetrics;
import io.opentelemetry.exporter.internal.ExporterMetricsAdapter;
import io.opentelemetry.exporter.internal.FailedExportException;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.HealthMetricLevel;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.SemConvAttributes;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
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
public final class GrpcExporter<T extends Marshaler> {

  private static final Logger internalLogger = Logger.getLogger(GrpcExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  // We only log unimplemented once since it's a configuration issue that won't be recovered.
  private final AtomicBoolean loggedUnimplemented = new AtomicBoolean();
  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final String type;
  private final GrpcSender<T> grpcSender;
  private final ExporterMetricsAdapter exporterMetrics;

  public GrpcExporter(
      String legacyExporterName,
      ExporterMetrics.Signal type,
      GrpcSender<T> grpcSender,
      HealthMetricLevel healthMetricLevel,
      ComponentId componentId,
      Supplier<MeterProvider> meterProviderSupplier,
      Attributes healthMetricAttributes) {
    this.type = type.toString();
    this.grpcSender = grpcSender;
    this.exporterMetrics =
        new ExporterMetricsAdapter(
            healthMetricLevel,
            meterProviderSupplier,
            type,
            componentId,
            healthMetricAttributes,
            legacyExporterName,
            "grpc");
  }

  public CompletableResultCode export(T exportRequest, int numItems) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    ExporterMetricsAdapter.Recording metricRecording =
        exporterMetrics.startRecordingExport(numItems);

    CompletableResultCode result = new CompletableResultCode();

    grpcSender.send(
        exportRequest,
        grpcResponse -> onResponse(result, metricRecording, grpcResponse),
        throwable -> onError(result, metricRecording, throwable));

    return result;
  }

  private void onResponse(
      CompletableResultCode result,
      ExporterMetricsAdapter.Recording metricRecording,
      GrpcResponse grpcResponse) {
    int statusCode = grpcResponse.grpcStatusValue();

    Attributes requestAttributes =
        Attributes.builder().put(SemConvAttributes.RPC_GRPC_STATUS_CODE, statusCode).build();

    if (statusCode == 0) {
      metricRecording.finishSuccessful(requestAttributes);
      result.succeed();
      return;
    }

    metricRecording.finishFailed("" + statusCode, requestAttributes);
    switch (statusCode) {
      case GRPC_STATUS_UNIMPLEMENTED:
        if (loggedUnimplemented.compareAndSet(false, true)) {
          GrpcExporterUtil.logUnimplemented(
              internalLogger, type, grpcResponse.grpcStatusDescription());
        }
        break;
      case GRPC_STATUS_UNAVAILABLE:
        logger.log(
            Level.SEVERE,
            "Failed to export "
                + type
                + "s. Server is UNAVAILABLE. "
                + "Make sure your collector is running and reachable from this network. "
                + "Full error message:"
                + grpcResponse.grpcStatusDescription());
        break;
      default:
        logger.log(
            Level.WARNING,
            "Failed to export "
                + type
                + "s. Server responded with gRPC status code "
                + statusCode
                + ". Error message: "
                + grpcResponse.grpcStatusDescription());
        break;
    }
    result.failExceptionally(FailedExportException.grpcFailedWithResponse(grpcResponse));
  }

  private void onError(
      CompletableResultCode result, ExporterMetricsAdapter.Recording metricRecording, Throwable e) {
    metricRecording.finishFailed(e, Attributes.empty());
    logger.log(
        Level.SEVERE,
        "Failed to export "
            + type
            + "s. The request could not be executed. Error message: "
            + e.getMessage(),
        e);
    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "Failed to export " + type + "s. Details follow: " + e);
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
