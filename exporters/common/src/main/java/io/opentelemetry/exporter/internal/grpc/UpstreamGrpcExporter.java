/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Status;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterMetrics;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link GrpcExporter} which uses the upstream grpc-java library.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class UpstreamGrpcExporter<T extends Marshaler> implements GrpcExporter<T> {

  private static final Logger internalLogger =
      Logger.getLogger(UpstreamGrpcExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  // We only log unavailable once since it's a configuration issue that won't be recovered.
  private final AtomicBoolean loggedUnimplemented = new AtomicBoolean();
  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final String type;
  private final ExporterMetrics exporterMetrics;
  private final MarshalerServiceStub<T, ?, ?> stub;
  private final long timeoutNanos;

  /** Creates a new {@link UpstreamGrpcExporter}. */
  UpstreamGrpcExporter(
      String exporterName,
      String type,
      MarshalerServiceStub<T, ?, ?> stub,
      Supplier<MeterProvider> meterProviderSupplier,
      long timeoutNanos) {
    this.type = type;
    this.exporterMetrics = ExporterMetrics.createGrpc(exporterName, type, meterProviderSupplier);
    this.timeoutNanos = timeoutNanos;
    this.stub = stub;
  }

  @Override
  public CompletableResultCode export(T exportRequest, int numItems) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    exporterMetrics.addSeen(numItems);

    CompletableResultCode result = new CompletableResultCode();

    MarshalerServiceStub<T, ?, ?> stub = this.stub;
    if (timeoutNanos > 0) {
      stub = stub.withDeadlineAfter(timeoutNanos, TimeUnit.NANOSECONDS);
    }
    Futures.addCallback(
        stub.export(exportRequest),
        new FutureCallback<Object>() {
          @Override
          public void onSuccess(@Nullable Object unused) {
            exporterMetrics.addSuccess(numItems);
            result.succeed();
          }

          @Override
          public void onFailure(Throwable t) {
            exporterMetrics.addFailed(numItems);
            Status status = Status.fromThrowable(t);
            switch (status.getCode()) {
              case UNIMPLEMENTED:
                if (loggedUnimplemented.compareAndSet(false, true)) {
                  GrpcExporterUtil.logUnimplemented(internalLogger, type, status.getDescription());
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
                        + status.getDescription());
                break;
              default:
                logger.log(
                    Level.WARNING,
                    "Failed to export "
                        + type
                        + "s. Server responded with gRPC status code "
                        + status.getCode().value()
                        + ". Error message: "
                        + status.getDescription());
                break;
            }
            if (logger.isLoggable(Level.FINEST)) {
              logger.log(Level.FINEST, "Failed to export " + type + "s. Details follow: " + t);
            }
            result.fail();
          }
        },
        MoreExecutors.directExecutor());

    return result;
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
    }
    return CompletableResultCode.ofSuccess();
  }
}
