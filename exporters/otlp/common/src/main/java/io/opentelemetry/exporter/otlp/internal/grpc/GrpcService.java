/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.net.URI;
import java.util.function.Function;
import java.util.function.Supplier;

public interface GrpcService<REQ extends Marshaler, RES extends Marshaler> {

  /** Returns a new {@link GrpcExporterBuilder}. */
  static <REQ extends Marshaler, RES extends Marshaler> GrpcServiceBuilder<REQ, RES> builder(
      String type,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      Supplier<Function<ManagedChannel, MarshalerServiceStub<REQ, ?, ?>>> stubFactory,
      String grpcServiceName,
      String grpcEndpointPath) {
    return GrpcServiceUtil.serviceBuilder(
        type, defaultTimeoutSecs, defaultEndpoint, stubFactory, grpcServiceName, grpcEndpointPath);
  }

  /**
   * Exports the {@code exportRequest} which is a request {@link Marshaler} for {@code numItems}
   * items.
   */
  RES export(REQ exportRequest);

  /** Shuts the exporter down. */
  CompletableResultCode shutdown();
}
