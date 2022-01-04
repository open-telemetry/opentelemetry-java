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

class GrpcServiceUtil {


  @SuppressWarnings("SystemOut")
  static <ReqT extends Marshaler, ResT extends UnMarshaller> GrpcServiceBuilder<ReqT, ResT> serviceBuilder(
      String type,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      Supplier<Function<ManagedChannel, MarshalerServiceStub<ReqT, ?, ?>>> stubFactory,
      String grpcServiceName,
      String grpcEndpointPath) {

    return new OkHttpGrpcServiceBuilder<>(
        type, grpcEndpointPath, defaultTimeoutSecs, defaultEndpoint);
  }

  private GrpcServiceUtil() {}
}
