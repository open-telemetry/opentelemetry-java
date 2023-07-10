/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.grpc.upstream.internal;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Status;
import io.opentelemetry.exporter.internal.grpc.GrpcResponse;
import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
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

  /** Creates a new {@link UpstreamGrpcSender}. */
  public UpstreamGrpcSender(MarshalerServiceStub<T, ?, ?> stub, long timeoutNanos) {
    this.timeoutNanos = timeoutNanos;
    this.stub = stub;
  }

  @Override
  public void send(T request, Runnable onSuccess, BiConsumer<GrpcResponse, Throwable> onError) {
    MarshalerServiceStub<T, ?, ?> stub = this.stub;
    if (timeoutNanos > 0) {
      stub = stub.withDeadlineAfter(timeoutNanos, TimeUnit.NANOSECONDS);
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
