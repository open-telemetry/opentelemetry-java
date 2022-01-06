/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.grpc.MarshalerServiceStub;
import java.net.URI;
import java.util.function.Function;
import java.util.function.Supplier;

final class GrpcServiceUtil {

  private static final boolean USE_OKHTTP;

  static {
    boolean useOkhttp = true;
    // Use the OkHttp exporter unless grpc-stub is on the classpath.
    try {
      Class.forName("io.grpc.stub.AbstractStub");
      useOkhttp = false;
    } catch (ClassNotFoundException e) {
      // Fall through
    }
    USE_OKHTTP = useOkhttp;
  }

  static <ReqT extends Marshaler, ResT extends UnMarshaller>
      GrpcServiceBuilder<ReqT, ResT> serviceBuilder(
          String type,
          long defaultTimeoutSecs,
          URI defaultEndpoint,
          Supplier<Function<ManagedChannel, MarshalerServiceStub<ReqT, ResT, ?>>> stubFactory,
          String grpcServiceName,
          String grpcEndpointPath) {
    if (USE_OKHTTP) {
      return new OkHttpGrpcServiceBuilder<>(
          type, grpcEndpointPath, defaultTimeoutSecs, defaultEndpoint);
    } else {
      return new DefaultGrpcServiceBuilder<>(
          type, stubFactory.get(), defaultTimeoutSecs, defaultEndpoint, grpcServiceName);
    }
  }

  private GrpcServiceUtil() {}
}
