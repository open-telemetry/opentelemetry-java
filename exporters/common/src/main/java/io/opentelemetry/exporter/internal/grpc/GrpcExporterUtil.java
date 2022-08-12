/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class GrpcExporterUtil {

  static void logUnimplemented(Logger logger, String type, @Nullable String fullErrorMessage) {
    String envVar;
    switch (type) {
      case "span":
        envVar = "OTEL_TRACES_EXPORTER";
        break;
      case "metric":
        envVar = "OTEL_METRICS_EXPORTER";
        break;
      case "log":
        envVar = "OTEL_LOGS_EXPORTER";
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
