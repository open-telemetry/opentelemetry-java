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
import io.opentelemetry.exporter.internal.grpc.MarshalerInputStream;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

class MarshallerRemoteSamplerServiceGrpc {

  private static final String SERVICE_NAME = "jaeger.api_v2.SamplingManager";

  private static final MethodDescriptor.Marshaller<SamplingStrategyParametersMarshaler>
      REQUEST_MARSHALLER =
          new MethodDescriptor.Marshaller<SamplingStrategyParametersMarshaler>() {
            @Override
            public InputStream stream(SamplingStrategyParametersMarshaler value) {
              return new MarshalerInputStream(value);
            }

            @Override
            public SamplingStrategyParametersMarshaler parse(InputStream stream) {
              throw new UnsupportedOperationException("Only for serializing");
            }
          };

  private static final MethodDescriptor.Marshaller<SamplingStrategyResponseUnMarshaler>
      RESPONSE_MARSHALLER =
          new MethodDescriptor.Marshaller<SamplingStrategyResponseUnMarshaler>() {
            @Override
            public InputStream stream(SamplingStrategyResponseUnMarshaler value) {
              throw new UnsupportedOperationException("Only for parsing");
            }

            @Override
            public SamplingStrategyResponseUnMarshaler parse(InputStream stream) {
              SamplingStrategyResponseUnMarshaler unmarshaller =
                  new SamplingStrategyResponseUnMarshaler();
              try {
                unmarshaller.read(readAllBytes(stream));
              } catch (IOException e) {
                // could not parse response
                throw new IllegalStateException(
                    "could not parse jaeger remote sampling response", e);
              }
              return unmarshaller;
            }
          };

  private static final MethodDescriptor<
          SamplingStrategyParametersMarshaler, SamplingStrategyResponseUnMarshaler>
      getPostSpansMethod =
          MethodDescriptor
              .<SamplingStrategyParametersMarshaler, SamplingStrategyResponseUnMarshaler>
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
          SamplingStrategyParametersMarshaler,
          SamplingStrategyResponseUnMarshaler,
          SamplingManagerFutureStub> {

    private SamplingManagerFutureStub(Channel channel, CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected SamplingManagerFutureStub build(Channel channel, CallOptions callOptions) {
      return new SamplingManagerFutureStub(channel, callOptions);
    }

    @Override
    public ListenableFuture<SamplingStrategyResponseUnMarshaler> export(
        SamplingStrategyParametersMarshaler request) {
      return ClientCalls.futureUnaryCall(
          getChannel().newCall(getPostSpansMethod, getCallOptions()), request);
    }
  }

  private static byte[] readAllBytes(InputStream inputStream) throws IOException {
    int bufLen = 4 * 0x400; // 4KB
    byte[] buf = new byte[bufLen];
    int readLen;
    IOException exception = null;

    try {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        while ((readLen = inputStream.read(buf, 0, bufLen)) != -1) {
          outputStream.write(buf, 0, readLen);
        }
        return outputStream.toByteArray();
      }
    } catch (IOException e) {
      exception = e;
      throw e;
    } finally {
      if (exception == null) {
        inputStream.close();
      } else {
        try {
          inputStream.close();
        } catch (IOException e) {
          exception.addSuppressed(e);
        }
      }
    }
  }

  private MarshallerRemoteSamplerServiceGrpc() {}
}
