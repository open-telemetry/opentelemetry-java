/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.grpc.managedchannel.internal;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.exporter.internal.grpc.GrpcResponse;
import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * A {@link GrpcSender} which uses the upstream grpc-java library.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class UpstreamGrpcSender<T extends Marshaler> implements GrpcSender<T> {

  private final MarshalerServiceStub<T, ?, ?> stub;
  private final boolean shutdownChannel;
  private final long timeoutNanos;
  private final Supplier<Map<String, List<String>>> headersSupplier;
  private final Executor executor;

  /** Creates a new {@link UpstreamGrpcSender}. */
  public UpstreamGrpcSender(
      MarshalerServiceStub<T, ?, ?> stub,
      boolean shutdownChannel,
      long timeoutNanos,
      Supplier<Map<String, List<String>>> headersSupplier,
      @Nullable ExecutorService executorService) {
    this.stub = stub;
    this.shutdownChannel = shutdownChannel;
    this.timeoutNanos = timeoutNanos;
    this.headersSupplier = headersSupplier;
    this.executor = executorService == null ? MoreExecutors.directExecutor() : executorService;
  }

  @Override
  public void send(T request, Consumer<GrpcResponse> onResponse, Consumer<Throwable> onError) {
    MarshalerServiceStub<T, ?, ?> stub = this.stub;
    if (timeoutNanos > 0) {
      stub = stub.withDeadlineAfter(Duration.ofNanos(timeoutNanos));
    }
    Map<String, List<String>> headers = headersSupplier.get();
    if (headers != null) {
      Metadata metadata = new Metadata();
      for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
        metadata.put(
            Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER),
            String.join(",", entry.getValue()));
      }
      stub = stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
    }

    Futures.addCallback(
        stub.export(request),
        new FutureCallback<Object>() {
          @Override
          public void onSuccess(@Nullable Object unused) {
            onResponse.accept(
                GrpcResponse.create(Status.OK.getCode().value(), Status.OK.getDescription()));
          }

          @Override
          public void onFailure(Throwable t) {
            Status status = fromThrowable(t);
            if (status == null) {
              onError.accept(t);
            } else {
              onResponse.accept(
                  GrpcResponse.create(status.getCode().value(), status.getDescription()));
            }
          }
        },
        executor);
  }

  /**
   * Copy of {@link Status#fromThrowable(Throwable)} which returns null instead of {@link
   * Status#UNKNOWN} when no status can be found.
   */
  @Nullable
  private static Status fromThrowable(Throwable cause) {
    while (cause != null) {
      if (cause instanceof StatusException) {
        return ((StatusException) cause).getStatus();
      } else if (cause instanceof StatusRuntimeException) {
        return ((StatusRuntimeException) cause).getStatus();
      }
      cause = cause.getCause();
    }
    return null;
  }

  @Override
  public CompletableResultCode shutdown() {
    if (shutdownChannel) {
      ManagedChannel channel = (ManagedChannel) stub.getChannel();
      channel.shutdownNow();
    }
    return CompletableResultCode.ofSuccess();
  }
}
