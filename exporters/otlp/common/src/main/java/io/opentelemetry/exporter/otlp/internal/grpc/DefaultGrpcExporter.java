/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Codec;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.opentelemetry.exporter.otlp.internal.ExporterMetrics;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link GrpcExporter} which uses the standard grpc-java library.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class DefaultGrpcExporter<T extends Marshaler> implements GrpcExporter<T> {

  private static final Logger internalLogger =
      Logger.getLogger(DefaultGrpcExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final String type;
  private final ExporterMetrics exporterMetrics;
  private final ManagedChannel managedChannel;
  private final MarshalerServiceStub<T, ?, ?> stub;
  private final long timeoutNanos;

  /** Creates a new {@link DefaultGrpcExporter}. */
  DefaultGrpcExporter(
      String type,
      ManagedChannel channel,
      MarshalerServiceStub<T, ?, ?> stub,
      long timeoutNanos,
      boolean compressionEnabled) {
    this.type = type;
    this.exporterMetrics = ExporterMetrics.createGrpc(type);
    this.managedChannel = channel;
    this.timeoutNanos = timeoutNanos;
    Codec codec = compressionEnabled ? new Codec.Gzip() : Codec.Identity.NONE;
    this.stub = stub.withCompression(codec.getMessageEncoding());
  }

  @Override
  public CompletableResultCode export(T exportRequest, int numItems) {
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
                logger.log(
                    Level.SEVERE,
                    "Failed to export "
                        + type
                        + "s. Server responded with UNIMPLEMENTED. "
                        + "This usually means that your collector is not configured with an otlp "
                        + "receiver in the \"pipelines\" section of the configuration. "
                        + "Full error message: "
                        + t.getMessage());
                break;
              case UNAVAILABLE:
                logger.log(
                    Level.SEVERE,
                    "Failed to export "
                        + type
                        + "s. Server is UNAVAILABLE. "
                        + "Make sure your collector is running and reachable from this network. "
                        + "Full error message:"
                        + t.getMessage());
                break;
              default:
                logger.log(
                    Level.WARNING,
                    "Failed to export " + type + "s. Error message: " + t.getMessage());
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
    if (managedChannel.isTerminated()) {
      return CompletableResultCode.ofSuccess();
    }
    exporterMetrics.unbind();
    return ManagedChannelUtil.shutdownChannel(managedChannel);
  }
}
