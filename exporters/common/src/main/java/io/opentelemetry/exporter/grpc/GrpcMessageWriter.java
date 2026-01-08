/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.grpc;

import java.io.IOException;
import java.io.OutputStream;

// TODO: have a single message writer abstraction
/** Writes gRPC messages to an {@link OutputStream}. */
public interface GrpcMessageWriter {

  /** Write the gRPC message bytes to the {@link OutputStream}. */
  void writeMessage(OutputStream output) throws IOException;

  /** Returns the message length in bytes. */
  int getContentLength();
}
