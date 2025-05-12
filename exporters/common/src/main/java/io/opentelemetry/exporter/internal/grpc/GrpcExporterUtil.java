/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import io.opentelemetry.exporter.internal.marshal.CodedInputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class GrpcExporterUtil {

  public static final int GRPC_STATUS_CANCELLED = 1;
  public static final int GRPC_STATUS_UNKNOWN = 2;
  public static final int GRPC_STATUS_DEADLINE_EXCEEDED = 4;
  public static final int GRPC_STATUS_RESOURCE_EXHAUSTED = 8;
  public static final int GRPC_STATUS_ABORTED = 10;
  public static final int GRPC_STATUS_OUT_OF_RANGE = 11;
  public static final int GRPC_STATUS_UNIMPLEMENTED = 12;
  public static final int GRPC_STATUS_UNAVAILABLE = 14;
  public static final int GRPC_STATUS_DATA_LOSS = 15;

  static void logUnimplemented(Logger logger, String type, @Nullable String fullErrorMessage) {

    // hopefully temporary special handling for profile signal as it evolves towards stability.
    if ("profile".equals(type)) {
      logger.log(
          Level.SEVERE,
          "Failed to export profile. The profile signal type is still under development "
              + "and the endpoint you are connecting to may not support it yet, "
              + "or may support a different version. "
              + "Full error message: "
              + fullErrorMessage);
      return;
    }

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

  /** Parses the message out of a serialized gRPC Status. */
  public static String getStatusMessage(byte[] serializedStatus) throws IOException {
    CodedInputStream input = CodedInputStream.newInstance(serializedStatus);
    boolean done = false;
    while (!done) {
      int tag = input.readTag();
      switch (tag) {
        case 0:
          done = true;
          break;
        case 18:
          return input.readStringRequireUtf8();
        default:
          input.skipField(tag);
          break;
      }
    }
    // Serialized Status proto had no message, proto always defaults to empty string when not found.
    return "";
  }
}
