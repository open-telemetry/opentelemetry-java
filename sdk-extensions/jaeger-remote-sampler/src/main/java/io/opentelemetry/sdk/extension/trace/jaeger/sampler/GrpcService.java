/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.net.URI;
import java.util.function.Function;
import java.util.function.Supplier;

interface GrpcService<ReqMarshalerT extends Marshaler, ResUnMarshalerT extends UnMarshaler> {

  /** Returns a new {@link GrpcExporterBuilder}. */
  static <ReqMarshalerT extends Marshaler, ResUnMarshalerT extends UnMarshaler>
      GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> builder(
          String type,
          long defaultTimeoutSecs,
          URI defaultEndpoint,
          Supplier<
                  Function<ManagedChannel, MarshalerServiceStub<ReqMarshalerT, ResUnMarshalerT, ?>>>
              stubFactory,
          String grpcServiceName,
          String grpcEndpointPath) {
    return GrpcServiceUtil.serviceBuilder(
        type, defaultTimeoutSecs, defaultEndpoint, stubFactory, grpcServiceName, grpcEndpointPath);
  }

  /**
   * Exports the {@code exportRequest} which is a request {@link Marshaler} for {@code numItems}
   * items.
   */
  ResUnMarshalerT execute(ReqMarshalerT request, ResUnMarshalerT response);

  /** Shuts the exporter down. */
  CompletableResultCode shutdown();
}
