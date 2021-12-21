/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
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

  static <REQ extends Marshaler, RES extends Marshaler> GrpcServiceBuilder<REQ, RES> serviceBuilder(
      String type,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      Supplier<Function<ManagedChannel, MarshalerServiceStub<REQ, ?, ?>>> stubFactory,
      String grpcServiceName,
      String grpcEndpointPath) {
    if (USE_OKHTTP) {
      System.out.println("Using OKHTTP");
      return new OkHttpGrpcServiceBuilder<>(
          type, grpcEndpointPath, defaultTimeoutSecs, defaultEndpoint);
    }
    System.out.println("---> returning null");
    return null;
  }

  private GrpcServiceUtil() {}
}
