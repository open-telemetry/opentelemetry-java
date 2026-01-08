/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.marshal;

import java.io.IOException;
import java.io.OutputStream;

/** Responsible for writing messages to an output stream. */
public interface MessageWriter {

  /** Write the message to the {@link OutputStream}. */
  void writeMessage(OutputStream output) throws IOException;

  /** Return the message length in bytes. */
  int getContentLength();
}
