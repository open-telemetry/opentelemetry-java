/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import java.net.URI;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class GrpcExporterUtil {

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

  static <T extends Marshaler> GrpcExporterBuilder<T> exporterBuilder(
      String exporterName,
      String type,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      Supplier<Function<ManagedChannel, MarshalerServiceStub<T, ?, ?>>> stubFactory,
      String grpcServiceName,
      String grpcEndpointPath) {
    if (USE_OKHTTP) {
      return new OkHttpGrpcExporterBuilder<>(
          exporterName, type, grpcEndpointPath, defaultTimeoutSecs, defaultEndpoint);
    } else {
      return new DefaultGrpcExporterBuilder<>(
          exporterName,
          type,
          stubFactory.get(),
          defaultTimeoutSecs,
          defaultEndpoint,
          grpcServiceName);
    }
  }

  static void logUnimplemented(Logger logger, String type, @Nullable String fullErrorMessage) {
    String envVar;
    switch (type) {
      case "span":
        envVar = "OTLP_TRACES_EXPORTER";
        break;
      case "metric":
        envVar = "OTLP_METRICS_EXPORTER";
        break;
      case "log":
        envVar = "OTLP_LOGS_EXPORTER";
        break;
      default:
        throw new IllegalStateException(
            "Unrecognized type, this is a programming bug in the OpenTelemetry SDK");
    }

    logger.log(
        Level.SEVERE,
        "Failed to export "
            + type
            + "s. Server responded with UNIMPLEMENTED. "
            + "This usually means that your collector is not configured with an otlp "
            + "receiver in the \"pipelines\" section of the configuration. "
            + "If export is not desired and you are using OpenTelemetry autoconfiguration or the javaagent, "
            + "disable export by setting "
            + envVar
            + "=none. "
            + "Full error message: "
            + fullErrorMessage);
  }

  private GrpcExporterUtil() {}
}
