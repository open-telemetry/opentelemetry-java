/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import static io.opentelemetry.exporter.internal.grpc.GrpcExporterUtil.GRPC_STATUS_UNAVAILABLE;
import static io.opentelemetry.exporter.internal.grpc.GrpcExporterUtil.GRPC_STATUS_UNIMPLEMENTED;
import static io.opentelemetry.exporter.internal.grpc.GrpcExporterUtil.GRPC_STATUS_UNKNOWN;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterMetrics;
import io.opentelemetry.exporter.internal.FailedExportException;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
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
  private final ExporterMetrics exporterMetrics;

  public GrpcExporter(
      String exporterName,
      String type,
      GrpcSender<T> grpcSender,
      Supplier<MeterProvider> meterProviderSupplier) {
    this.type = type;
    this.grpcSender = grpcSender;
    this.exporterMetrics = ExporterMetrics.createGrpc(exporterName, type, meterProviderSupplier);
  }

  public CompletableResultCode export(T exportRequest, int numItems) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    exporterMetrics.addSeen(numItems);

    CompletableResultCode result = new CompletableResultCode();

    grpcSender.send(
        exportRequest,
        () -> {
          exporterMetrics.addSuccess(numItems);
          result.succeed();
        },
        (response, throwable) -> {
          exporterMetrics.addFailed(numItems);

          logFailureMessage(response, throwable);

          switch (response.grpcStatusValue()) {
            case GRPC_STATUS_UNKNOWN:
              result.failExceptionally(FailedExportException.grpcFailedExceptionally(throwable));
              break;
            default:
              result.failExceptionally(FailedExportException.grpcFailedWithResponse(response));
              break;
          }
        });

    return result;
  }

  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
      return CompletableResultCode.ofSuccess();
    }
    return grpcSender.shutdown();
  }

  private void logFailureMessage(GrpcResponse response, Throwable throwable) {
    switch (response.grpcStatusValue()) {
      case GRPC_STATUS_UNIMPLEMENTED:
        if (loggedUnimplemented.compareAndSet(false, true)) {
          GrpcExporterUtil.logUnimplemented(internalLogger, type, response.grpcStatusDescription());
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
                + response.grpcStatusDescription());
        break;
      default:
        logger.log(
            Level.WARNING,
            "Failed to export "
                + type
                + "s. Server responded with gRPC status code "
                + response.grpcStatusValue()
                + ". Error message: "
                + response.grpcStatusDescription());
        break;
    }

    if (logger.isLoggable(Level.FINEST)) {
      logger.log(Level.FINEST, "Failed to export " + type + "s. Details follow: " + throwable);
    }
  }
}
