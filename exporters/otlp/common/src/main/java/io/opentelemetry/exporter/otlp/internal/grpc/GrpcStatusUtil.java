/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import com.google.protobuf.CodedInputStream;
import java.io.IOException;

/**
 * Utilities for working with gRPC status without requiring dependencies on gRPC.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class GrpcStatusUtil {

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

  private GrpcStatusUtil() {}
}
