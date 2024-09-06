/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/** An {@link OutputStream} that ignores calls to {@link #close()}. */
class IgnoreCloseOutputStream extends FilterOutputStream {

  public IgnoreCloseOutputStream(OutputStream outputStream) {
    super(outputStream);
  }

  @SuppressWarnings("SystemOut")
  @Override
  public void close() throws IOException {
    if (out == System.out || out == System.err) {
      // close() on System.out and System.err breaks the output stream, so we ignore it
      flush();
    } else {
      super.close();
    }
  }
}
