/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import static io.grpc.MethodDescriptor.generateFullMethodName;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import io.opentelemetry.exporter.otlp.internal.grpc.MarshalerInputStream;
import io.opentelemetry.exporter.otlp.internal.grpc.MarshalerServiceStub;
import java.io.IOException;
import java.io.InputStream;

class MarshallerRemoteSamplerServiceGrpc {

  private static final String SERVICE_NAME = "jaeger.api_v2.SamplingManager";

  private static final MethodDescriptor.Marshaller<SamplingStrategyParametersMarshaller>
      REQUEST_MARSHALLER =
          new MethodDescriptor.Marshaller<SamplingStrategyParametersMarshaller>() {
            @Override
            public InputStream stream(SamplingStrategyParametersMarshaller value) {
              return new MarshalerInputStream(value);
            }

            @Override
            public SamplingStrategyParametersMarshaller parse(InputStream stream) {
              throw new UnsupportedOperationException("Only for serializing");
            }
          };

  private static final MethodDescriptor.Marshaller<SamplingStrategyResponseUnMarshaller>
      RESPONSE_MARSHALLER =
          new MethodDescriptor.Marshaller<SamplingStrategyResponseUnMarshaller>() {
            @Override
            public InputStream stream(SamplingStrategyResponseUnMarshaller value) {
              throw new UnsupportedOperationException("Only for parsing");
            }

            @Override
            public SamplingStrategyResponseUnMarshaller parse(InputStream stream) {
              SamplingStrategyResponseUnMarshaller unmarshaller =
                  new SamplingStrategyResponseUnMarshaller();
              try {
                unmarshaller.read(stream);
              } catch (IOException e) {
                // could not parse response
                throw new IllegalStateException(
                    "could not parse jaeger remote sampling response", e);
              }
              return unmarshaller;
            }
          };

  private static final MethodDescriptor<
          SamplingStrategyParametersMarshaller, SamplingStrategyResponseUnMarshaller>
      getPostSpansMethod =
          MethodDescriptor
              .<SamplingStrategyParametersMarshaller, SamplingStrategyResponseUnMarshaller>
                  newBuilder()
              .setType(MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetSamplingStrategy"))
              .setRequestMarshaller(REQUEST_MARSHALLER)
              .setResponseMarshaller(RESPONSE_MARSHALLER)
              .build();

  static SamplingManagerFutureStub newFutureStub(Channel channel) {
    return SamplingManagerFutureStub.newStub(SamplingManagerFutureStub::new, channel);
  }

  static final class SamplingManagerFutureStub
      extends MarshalerServiceStub<
          SamplingStrategyParametersMarshaller,
          SamplingStrategyResponseUnMarshaller,
          SamplingManagerFutureStub> {

    private SamplingManagerFutureStub(Channel channel, CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SamplingManagerFutureStub build(Channel channel, CallOptions callOptions) {
      return new SamplingManagerFutureStub(channel, callOptions);
    }

    @Override
    public ListenableFuture<SamplingStrategyResponseUnMarshaller> export(
        SamplingStrategyParametersMarshaller request) {
      return ClientCalls.futureUnaryCall(
          getChannel().newCall(getPostSpansMethod, getCallOptions()), request);
    }
  }

  private MarshallerRemoteSamplerServiceGrpc() {}
}
