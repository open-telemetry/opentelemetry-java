/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import com.google.common.util.concurrent.Futures;
import io.grpc.CallOptions;
import io.grpc.ManagedChannel;
import io.grpc.MethodDescriptor;
import io.grpc.Status;
import io.grpc.stub.ClientCalls;
import io.opentelemetry.exporter.internal.grpc.ManagedChannelUtil;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.time.Duration;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

final class UpstreamGrpcService implements GrpcService {

  private static final Logger logger = Logger.getLogger(UpstreamGrpcService.class.getName());

  private final String type;
  private final ManagedChannel managedChannel;
  private final MethodDescriptor<
          SamplingStrategyParametersMarshaler, SamplingStrategyResponseUnMarshaler>
      methodDescriptor;
  private final long timeoutNanos;

  /** Creates a new {@link UpstreamGrpcService}. */
  UpstreamGrpcService(
      String type,
      ManagedChannel channel,
      MethodDescriptor<SamplingStrategyParametersMarshaler, SamplingStrategyResponseUnMarshaler>
          methodDescriptor,
      long timeoutNanos) {
    this.type = type;
    this.managedChannel = channel;
    this.timeoutNanos = timeoutNanos;
    this.methodDescriptor = methodDescriptor;
  }

  @Override
  public SamplingStrategyResponseUnMarshaler execute(
      SamplingStrategyParametersMarshaler exportRequest,
      SamplingStrategyResponseUnMarshaler responseUnmarshaller) {

    CallOptions callOptions = CallOptions.DEFAULT;

    if (timeoutNanos > 0) {
      callOptions = callOptions.withDeadlineAfter(Duration.ofNanos(timeoutNanos));
    }

    try {
      return Objects.requireNonNull(
          Futures.getUnchecked(
              ClientCalls.futureUnaryCall(
                  managedChannel.newCall(methodDescriptor, callOptions), exportRequest)));
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
