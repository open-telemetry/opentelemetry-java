/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import static io.grpc.MethodDescriptor.generateFullMethodName;

import io.grpc.MethodDescriptor;
import io.opentelemetry.exporter.otlp.internal.grpc.MarshalerInputStream;
import java.io.InputStream;

// Adapted from the protoc generated code for CollectorServiceGrpc.
final class MarshalerCollectorServiceGrpc {

  private static final String SERVICE_NAME = "jaeger.api_v2.CollectorService";

  private static final MethodDescriptor.Marshaller<PostSpansRequestMarshaler> REQUEST_MARSHALLER =
      new MethodDescriptor.Marshaller<PostSpansRequestMarshaler>() {
        @Override
        public InputStream stream(PostSpansRequestMarshaler value) {
          return new MarshalerInputStream(value);
        }

        @Override
        public PostSpansRequestMarshaler parse(InputStream stream) {
          throw new UnsupportedOperationException("Only for serializing");
        }
      };

  private static final MethodDescriptor.Marshaller<PostSpansResponse> RESPONSE_MARSHALER =
      new MethodDescriptor.Marshaller<PostSpansResponse>() {
        @Override
        public InputStream stream(PostSpansResponse value) {
          throw new UnsupportedOperationException("Only for parsing");
        }

        @Override
        public PostSpansResponse parse(InputStream stream) {
          return PostSpansResponse.INSTANCE;
        }
      };

  private static volatile MethodDescriptor<PostSpansRequestMarshaler, PostSpansResponse>
      getPostSpansMethod;

  static CollectorServiceFutureStub newFutureStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<CollectorServiceFutureStub> factory =
        new io.grpc.stub.AbstractStub.StubFactory<CollectorServiceFutureStub>() {
          @Override
          public CollectorServiceFutureStub newStub(
              io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
            return new CollectorServiceFutureStub(channel, callOptions);
          }
        };
    return CollectorServiceFutureStub.newStub(factory, channel);
  }

  static final class CollectorServiceFutureStub
      extends io.grpc.stub.AbstractFutureStub<
          MarshalerCollectorServiceGrpc.CollectorServiceFutureStub> {
    private CollectorServiceFutureStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MarshalerCollectorServiceGrpc.CollectorServiceFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new MarshalerCollectorServiceGrpc.CollectorServiceFutureStub(channel, callOptions);
    }

    com.google.common.util.concurrent.ListenableFuture<PostSpansResponse> postSpans(
        PostSpansRequestMarshaler request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPostSpansMethod(), getCallOptions()), request);
    }
  }

  private static MethodDescriptor<PostSpansRequestMarshaler, PostSpansResponse>
      getPostSpansMethod() {
    MethodDescriptor<PostSpansRequestMarshaler, PostSpansResponse> getPostSpansMethod;
    if ((getPostSpansMethod = MarshalerCollectorServiceGrpc.getPostSpansMethod) == null) {
      synchronized (MarshalerCollectorServiceGrpc.class) {
        if ((getPostSpansMethod = MarshalerCollectorServiceGrpc.getPostSpansMethod) == null) {
          MarshalerCollectorServiceGrpc.getPostSpansMethod =
              getPostSpansMethod =
                  MethodDescriptor.<PostSpansRequestMarshaler, PostSpansResponse>newBuilder()
                      .setType(MethodDescriptor.MethodType.UNARY)
                      .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PostSpans"))
                      .setRequestMarshaller(REQUEST_MARSHALLER)
                      .setResponseMarshaller(RESPONSE_MARSHALER)
                      .build();
        }
      }
    }
    return getPostSpansMethod;
  }

  private MarshalerCollectorServiceGrpc() {}
}
