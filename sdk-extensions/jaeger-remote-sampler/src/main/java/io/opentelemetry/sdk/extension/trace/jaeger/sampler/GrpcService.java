/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.net.URI;

interface GrpcService<ReqMarshalerT extends Marshaler, ResUnMarshalerT extends UnMarshaler> {

  /** Returns a new {@link GrpcExporterBuilder}. */
  static <ReqMarshalerT extends Marshaler, ResUnMarshalerT extends UnMarshaler>
      GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> builder(
          String type, long defaultTimeoutSecs, URI defaultEndpoint, String grpcEndpointPath) {
    return GrpcServiceUtil.serviceBuilder(
        type, defaultTimeoutSecs, defaultEndpoint, grpcEndpointPath);
  }

  /**
   * Exports the {@code exportRequest} which is a request {@link Marshaler} for {@code numItems}
   * items.
   */
  void execute(ReqMarshalerT request, ResUnMarshalerT response);

  /** Shuts the exporter down. */
  CompletableResultCode shutdown();
}
