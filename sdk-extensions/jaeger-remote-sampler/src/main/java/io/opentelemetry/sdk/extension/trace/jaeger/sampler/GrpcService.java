/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.net.URI;
import java.util.function.Function;
import java.util.function.Supplier;

interface GrpcService<ReqT extends Marshaler, ResT extends UnMarshaller> {

  /** Returns a new {@link GrpcExporterBuilder}. */
  static <ReqT extends Marshaler, ResT extends UnMarshaller> GrpcServiceBuilder<ReqT, ResT> builder(
      String type,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      Supplier<Function<ManagedChannel, MarshalerServiceStub<ReqT, ResT, ?>>> stubFactory,
      String grpcServiceName,
      String grpcEndpointPath) {
    return GrpcServiceUtil.serviceBuilder(
        type, defaultTimeoutSecs, defaultEndpoint, stubFactory, grpcServiceName, grpcEndpointPath);
  }

  /**
   * Exports the {@code exportRequest} which is a request {@link Marshaler} for {@code numItems}
   * items.
   */
  ResT execute(ReqT request, ResT response);

  /** Shuts the exporter down. */
  CompletableResultCode shutdown();
}
