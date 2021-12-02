/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import static io.grpc.MethodDescriptor.generateFullMethodName;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import io.opentelemetry.exporter.otlp.internal.grpc.MarshalerInputStream;
import io.opentelemetry.exporter.otlp.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.otlp.internal.logs.LogsRequestMarshaler;
import java.io.InputStream;

/**
 * Adapted from the protoc generated code for LogsServiceGrpc.
 */
final class MarshalerLogsServiceGrpc {

  private static final String SERVICE_NAME = "opentelemetry.proto.collector.logs.v1.LogsService";

  private static final MethodDescriptor.Marshaller<LogsRequestMarshaler> REQUEST_MARSHALLER =
      new MethodDescriptor.Marshaller<LogsRequestMarshaler>() {
        @Override
        public InputStream stream(LogsRequestMarshaler value) {
          return new MarshalerInputStream(value);
        }

        @Override
        public LogsRequestMarshaler parse(InputStream stream) {
          throw new UnsupportedOperationException("Only for serializing");
        }
      };

  private static final MethodDescriptor.Marshaller<ExportLogsServiceResponse> RESPONSE_MARSHALER =
      new MethodDescriptor.Marshaller<ExportLogsServiceResponse>() {
        @Override
        public InputStream stream(ExportLogsServiceResponse value) {
          throw new UnsupportedOperationException("Only for parsing");
        }

        @Override
        public ExportLogsServiceResponse parse(InputStream stream) {
          return ExportLogsServiceResponse.INSTANCE;
        }
      };

  private static final MethodDescriptor<LogsRequestMarshaler, ExportLogsServiceResponse>
      getExportMethod =
          MethodDescriptor.<LogsRequestMarshaler, ExportLogsServiceResponse>newBuilder()
              .setType(MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Export"))
              .setRequestMarshaller(REQUEST_MARSHALLER)
              .setResponseMarshaller(RESPONSE_MARSHALER)
              .build();

  static LogsServiceFutureStub newFutureStub(Channel channel) {
    return LogsServiceFutureStub.newStub(LogsServiceFutureStub::new, channel);
  }

  static final class LogsServiceFutureStub
      extends MarshalerServiceStub<
          LogsRequestMarshaler, ExportLogsServiceResponse, LogsServiceFutureStub> {
    private LogsServiceFutureStub(Channel channel, CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MarshalerLogsServiceGrpc.LogsServiceFutureStub build(
        Channel channel, CallOptions callOptions) {
      return new MarshalerLogsServiceGrpc.LogsServiceFutureStub(channel, callOptions);
    }

    @Override
    public ListenableFuture<ExportLogsServiceResponse> export(LogsRequestMarshaler request) {
      return ClientCalls.futureUnaryCall(
          getChannel().newCall(getExportMethod, getCallOptions()), request);
    }
  }

  private MarshalerLogsServiceGrpc() {}
}
