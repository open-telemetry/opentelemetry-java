/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.http;

import java.io.IOException;
import java.io.OutputStream;

/** Writes HTTP request bodies to an {@link OutputStream}. */
public interface HttpRequestBodyWriter {

  /** Write the gRPC message bytes to the {@link OutputStream}. */
  void writeRequestBody(OutputStream output) throws IOException;

  /** Return the request body length in bytes. */
  int contentLength();
}
