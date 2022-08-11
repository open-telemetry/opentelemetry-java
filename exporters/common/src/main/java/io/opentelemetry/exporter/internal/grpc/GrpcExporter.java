/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import io.grpc.Channel;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.net.URI;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * An exporter of a {@link Marshaler} using the gRPC wire format.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface GrpcExporter<T extends Marshaler> {

  /** Returns a new {@link GrpcExporterBuilder}. */
  static <T extends Marshaler> GrpcExporterBuilder<T> builder(
      String exporterName,
      String type,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>> stubFactory,
      String grpcEndpointPath) {
    return new GrpcExporterBuilder<>(
        exporterName, type, defaultTimeoutSecs, defaultEndpoint, stubFactory, grpcEndpointPath);
  }

  /**
   * Exports the {@code exportRequest} which is a request {@link Marshaler} for {@code numItems}
   * items.
   */
  CompletableResultCode export(T exportRequest, int numItems);

  /** Shuts the exporter down. */
  CompletableResultCode shutdown();
}
