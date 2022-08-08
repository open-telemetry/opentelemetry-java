/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static io.grpc.MethodDescriptor.generateFullMethodName;

import io.grpc.MethodDescriptor;
import io.opentelemetry.exporter.internal.grpc.MarshalerInputStream;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import java.io.InputStream;
import javax.annotation.Nullable;

// Adapted from the protoc generated code for TraceServiceGrpc.
final class MarshalerTraceServiceGrpc {

  private static final String SERVICE_NAME = "opentelemetry.proto.collector.trace.v1.TraceService";

  private static final MethodDescriptor.Marshaller<TraceRequestMarshaler> REQUEST_MARSHALLER =
      new MethodDescriptor.Marshaller<TraceRequestMarshaler>() {
        @Override
        public InputStream stream(TraceRequestMarshaler value) {
          return new MarshalerInputStream(value);
        }

        @Override
        public TraceRequestMarshaler parse(InputStream stream) {
          throw new UnsupportedOperationException("Only for serializing");
        }
      };

  private static final MethodDescriptor.Marshaller<ExportTraceServiceResponse> RESPONSE_MARSHALER =
      new MethodDescriptor.Marshaller<ExportTraceServiceResponse>() {
        @Override
        public InputStream stream(ExportTraceServiceResponse value) {
          throw new UnsupportedOperationException("Only for parsing");
        }

        @Override
        public ExportTraceServiceResponse parse(InputStream stream) {
          return ExportTraceServiceResponse.INSTANCE;
        }
      };

  private static final io.grpc.MethodDescriptor<TraceRequestMarshaler, ExportTraceServiceResponse>
      getExportMethod =
          io.grpc.MethodDescriptor.<TraceRequestMarshaler, ExportTraceServiceResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Export"))
              .setRequestMarshaller(REQUEST_MARSHALLER)
              .setResponseMarshaller(RESPONSE_MARSHALER)
              .build();

  static TraceServiceFutureStub newFutureStub(
      io.grpc.Channel channel, @Nullable String authorityOverride) {
    return TraceServiceFutureStub.newStub(
        (c, options) -> new TraceServiceFutureStub(c, options.withAuthority(authorityOverride)),
        channel);
  }

  static final class TraceServiceFutureStub
      extends MarshalerServiceStub<
          TraceRequestMarshaler, ExportTraceServiceResponse, TraceServiceFutureStub> {
    private TraceServiceFutureStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected MarshalerTraceServiceGrpc.TraceServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MarshalerTraceServiceGrpc.TraceServiceFutureStub(channel, callOptions);
    }

    @Override
    public com.google.common.util.concurrent.ListenableFuture<ExportTraceServiceResponse> export(
        TraceRequestMarshaler request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExportMethod, getCallOptions()), request);
    }
  }

  private MarshalerTraceServiceGrpc() {}
}
