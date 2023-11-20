/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.grpc.managedchannel.internal;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.exporter.internal.grpc.GrpcResponse;
import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A {@link GrpcSender} which uses the upstream grpc-java library.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class UpstreamGrpcSender<T extends Marshaler> implements GrpcSender<T> {

  private final MarshalerServiceStub<T, ?, ?> stub;
  private final long timeoutNanos;
  private final Supplier<Map<String, String>> headersSupplier;

  /** Creates a new {@link UpstreamGrpcSender}. */
  public UpstreamGrpcSender(
      MarshalerServiceStub<T, ?, ?> stub,
      long timeoutNanos,
      Supplier<Map<String, String>> headersSupplier) {
    this.timeoutNanos = timeoutNanos;
    this.stub = stub;
    this.headersSupplier = headersSupplier;
  }

  @Override
  public void send(T request, Runnable onSuccess, BiConsumer<GrpcResponse, Throwable> onError) {
    MarshalerServiceStub<T, ?, ?> stub = this.stub;
    if (timeoutNanos > 0) {
      stub = stub.withDeadlineAfter(timeoutNanos, TimeUnit.NANOSECONDS);
    }
    Map<String, String> headers = headersSupplier.get();
    if (headers != null) {
      Metadata metadata = new Metadata();
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        metadata.put(
            Metadata.Key.of(entry.getKey(), Metadata.ASCII_STRING_MARSHALLER), entry.getValue());
      }
      stub = stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
    }

    Futures.addCallback(
        stub.export(request),
        new FutureCallback<Object>() {
          @Override
          public void onSuccess(@Nullable Object unused) {
            onSuccess.run();
          }

          @Override
          public void onFailure(Throwable t) {
            Status status = Status.fromThrowable(t);
            onError.accept(
                GrpcResponse.create(status.getCode().value(), status.getDescription()), t);
          }
        },
        MoreExecutors.directExecutor());
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
  }
}
