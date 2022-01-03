/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.UnMarshaller;
import java.net.URI;
import java.util.function.Function;
import java.util.function.Supplier;

public class GrpcServiceUtil {

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

  @SuppressWarnings("SystemOut")
  static <ReqT extends Marshaler, ResT extends UnMarshaller> GrpcServiceBuilder<ReqT, ResT> serviceBuilder(
      String type,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      Supplier<Function<ManagedChannel, MarshalerServiceStub<ReqT, ?, ?>>> stubFactory,
      String grpcServiceName,
      String grpcEndpointPath) {
    if (USE_OKHTTP) {
      System.out.println("Using OKHTTP");
    }
    // TODO return default builder
    return new OkHttpGrpcServiceBuilder<>(
        type, grpcEndpointPath, defaultTimeoutSecs, defaultEndpoint);
  }

  private GrpcServiceUtil() {}
}
