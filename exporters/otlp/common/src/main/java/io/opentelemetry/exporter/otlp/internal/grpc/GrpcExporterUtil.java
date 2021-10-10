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

final class GrpcExporterUtil {

  private static final boolean USE_OKHTTP;

  static {
    boolean useOkhttp = false;
    // Use the OkHttp exporter if ManagedChannel is not found and OkHttp is.
    try {
      Class.forName("io.grpc.ManagedChannel");
    } catch (ClassNotFoundException e) {
      try {
        Class.forName("okhttp3.OkHttpClient");
        useOkhttp = true;
      } catch (ClassNotFoundException classNotFoundException) {
        // Fall through
      }
    }
    USE_OKHTTP = useOkhttp;
  }

  static <T extends Marshaler> GrpcExporterBuilder<T> exporterBuilder(
      String type,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      Supplier<Function<ManagedChannel, MarshalerServiceStub<T, ?, ?>>> stubFactory,
      String grpcEndpointPath) {
    if (USE_OKHTTP) {
      return new OkHttpGrpcExporterBuilder<>(
          type, grpcEndpointPath, defaultTimeoutSecs, defaultEndpoint);
    } else {
      return new DefaultGrpcExporterBuilder<>(
          type, stubFactory.get(), defaultTimeoutSecs, defaultEndpoint);
    }
  }

  private GrpcExporterUtil() {}
}
