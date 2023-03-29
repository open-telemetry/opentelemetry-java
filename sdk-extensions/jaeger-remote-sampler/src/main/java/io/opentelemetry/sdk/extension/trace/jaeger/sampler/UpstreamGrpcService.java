/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import com.google.common.util.concurrent.Futures;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.opentelemetry.exporter.internal.grpc.ManagedChannelUtil;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

final class UpstreamGrpcService implements GrpcService {

  private static final Logger logger = Logger.getLogger(UpstreamGrpcService.class.getName());

  private final String type;
  private final ManagedChannel managedChannel;
  private final MarshalerServiceStub<
          SamplingStrategyParametersMarshaler, SamplingStrategyResponseUnMarshaler, ?>
      stub;
  private final long timeoutNanos;

  /** Creates a new {@link UpstreamGrpcService}. */
  UpstreamGrpcService(
      String type,
      ManagedChannel channel,
      MarshalerServiceStub<
              SamplingStrategyParametersMarshaler, SamplingStrategyResponseUnMarshaler, ?>
          stub,
      long timeoutNanos) {
    this.type = type;
    this.managedChannel = channel;
    this.timeoutNanos = timeoutNanos;
    this.stub = stub;
  }

  @Override
  public SamplingStrategyResponseUnMarshaler execute(
      SamplingStrategyParametersMarshaler exportRequest,
      SamplingStrategyResponseUnMarshaler responseUnmarshaller) {
    MarshalerServiceStub<
            SamplingStrategyParametersMarshaler, SamplingStrategyResponseUnMarshaler, ?>
        stub = this.stub;
    if (timeoutNanos > 0) {
      stub = stub.withDeadlineAfter(timeoutNanos, TimeUnit.NANOSECONDS);
    }

    try {
      return Futures.getUnchecked(stub.export(exportRequest));
    } catch (Throwable t) {
      Status status = Status.fromThrowable(t);

      if (status.getCode().equals(Status.Code.UNIMPLEMENTED)) {
        logger.log(
            Level.SEVERE,
            "Failed to execute "
                + type
                + "s. Server responded with UNIMPLEMENTED. "
                + "Full error message: "
                + status.getDescription());
      } else if (status.getCode().equals(Status.Code.UNAVAILABLE)) {
        logger.log(
            Level.SEVERE,
            "Failed to execute "
                + type
                + "s. Server is UNAVAILABLE. "
                + "Make sure your service is running and reachable from this network. "
                + "Full error message:"
                + status.getDescription());
      } else {
        logger.log(
            Level.WARNING,
            "Failed to execute "
                + type
                + "s. Server responded with gRPC status code "
                + status.getCode().value()
                + ". Error message: "
                + status.getDescription());
      }
    }

    return responseUnmarshaller;
  }

  @Override
  public CompletableResultCode shutdown() {
    if (managedChannel.isTerminated()) {
      return CompletableResultCode.ofSuccess();
    }
    return ManagedChannelUtil.shutdownChannel(managedChannel);
  }
}
