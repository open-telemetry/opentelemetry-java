/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static io.grpc.MethodDescriptor.generateFullMethodName;

import io.grpc.MethodDescriptor;
import io.opentelemetry.exporter.otlp.internal.grpc.MarshalerInputStream;
import io.opentelemetry.exporter.otlp.internal.metrics.MetricsRequestMarshaler;
import java.io.InputStream;

// Adapted from the protoc generated code for MetricsServiceGrpc.
final class MarshalerMetricsServiceGrpc {

  private static final String SERVICE_NAME =
      "opentelemetry.proto.collector.metrics.v1.MetricsService";

  private static final MethodDescriptor.Marshaller<MetricsRequestMarshaler> REQUEST_MARSHALLER =
      new MethodDescriptor.Marshaller<MetricsRequestMarshaler>() {
        @Override
        public InputStream stream(MetricsRequestMarshaler value) {
          return new MarshalerInputStream(value);
        }

        @Override
        public MetricsRequestMarshaler parse(InputStream stream) {
          throw new UnsupportedOperationException("Only for serializing");
        }
      };

  private static final MethodDescriptor.Marshaller<ExportMetricsServiceResponse>
      RESPONSE_MARSHALER =
          new MethodDescriptor.Marshaller<ExportMetricsServiceResponse>() {
            @Override
            public InputStream stream(ExportMetricsServiceResponse value) {
              throw new UnsupportedOperationException("Only for parsing");
            }

            @Override
            public ExportMetricsServiceResponse parse(InputStream stream) {
              return ExportMetricsServiceResponse.INSTANCE;
            }
          };

  private static final MethodDescriptor<MetricsRequestMarshaler, ExportMetricsServiceResponse>
      getExportMethod =
          MethodDescriptor.<MetricsRequestMarshaler, ExportMetricsServiceResponse>newBuilder()
              .setType(MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Export"))
              .setRequestMarshaller(REQUEST_MARSHALLER)
              .setResponseMarshaller(RESPONSE_MARSHALER)
              .build();

  static MetricsServiceFutureStub newFutureStub(io.grpc.Channel channel) {
    return MetricsServiceFutureStub.newStub(MetricsServiceFutureStub::new, channel);
  }

  static final class MetricsServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<
          MarshalerMetricsServiceGrpc.MetricsServiceFutureStub> {
    private MetricsServiceFutureStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MarshalerMetricsServiceGrpc.MetricsServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MarshalerMetricsServiceGrpc.MetricsServiceFutureStub(channel, callOptions);
    }

    com.google.common.util.concurrent.ListenableFuture<ExportMetricsServiceResponse> export(
        MetricsRequestMarshaler request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getExportMethod, getCallOptions()), request);
    }
  }

  private MarshalerMetricsServiceGrpc() {}
}
