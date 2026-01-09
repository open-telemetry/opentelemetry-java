/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import static io.grpc.MethodDescriptor.generateFullMethodName;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.MethodDescriptor;
import io.grpc.stub.ClientCalls;
import io.opentelemetry.exporter.internal.grpc.MarshalerInputStream;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import java.io.InputStream;
import javax.annotation.Nullable;

// Adapted from the protoc generated code for ProfilesServiceGrpc.
final class MarshalerProfilesServiceGrpc {

  private static final String SERVICE_NAME =
      "opentelemetry.proto.collector.profiles.v1development.ProfilesService";

  private static final MethodDescriptor.Marshaller<Marshaler> REQUEST_MARSHALLER =
      new MethodDescriptor.Marshaller<Marshaler>() {
        @Override
        public InputStream stream(Marshaler value) {
          return new MarshalerInputStream(value);
        }

        @Override
        public Marshaler parse(InputStream stream) {
          throw new UnsupportedOperationException("Only for serializing");
        }
      };

  private static final MethodDescriptor.Marshaller<ExportProfilesServiceResponse>
      RESPONSE_MARSHALER =
          new MethodDescriptor.Marshaller<ExportProfilesServiceResponse>() {
            @Override
            public InputStream stream(ExportProfilesServiceResponse value) {
              throw new UnsupportedOperationException("Only for parsing");
            }

            @Override
            public ExportProfilesServiceResponse parse(InputStream stream) {
              return ExportProfilesServiceResponse.INSTANCE;
            }
          };

  private static final MethodDescriptor<Marshaler, ExportProfilesServiceResponse> getExportMethod =
      MethodDescriptor.<Marshaler, ExportProfilesServiceResponse>newBuilder()
          .setType(MethodDescriptor.MethodType.UNARY)
          .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Export"))
          .setRequestMarshaller(REQUEST_MARSHALLER)
          .setResponseMarshaller(RESPONSE_MARSHALER)
          .build();

  static ProfilesServiceFutureStub newFutureStub(
      Channel channel, @Nullable String authorityOverride) {
    return ProfilesServiceFutureStub.newStub(
        (c, options) -> new ProfilesServiceFutureStub(c, options.withAuthority(authorityOverride)),
        channel);
  }

  static final class ProfilesServiceFutureStub
      extends MarshalerServiceStub<
          Marshaler, ExportProfilesServiceResponse, ProfilesServiceFutureStub> {
    private ProfilesServiceFutureStub(Channel channel, CallOptions callOptions) {
      super(channel, callOptions);
    }

    @Override
    protected MarshalerProfilesServiceGrpc.ProfilesServiceFutureStub build(
        Channel channel, CallOptions callOptions) {
      return new MarshalerProfilesServiceGrpc.ProfilesServiceFutureStub(channel, callOptions);
    }

    @Override
    public ListenableFuture<ExportProfilesServiceResponse> export(Marshaler request) {
      return ClientCalls.futureUnaryCall(
          getChannel().newCall(getExportMethod, getCallOptions()), request);
    }
  }

  private MarshalerProfilesServiceGrpc() {}
}
