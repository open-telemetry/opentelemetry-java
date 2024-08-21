/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.writer;

import java.io.FilterOutputStream;
import java.io.OutputStream;

/** An {@link OutputStream} that ignores calls to {@link #close()}. */
class IgnoreCloseOutputStream extends FilterOutputStream {

  public IgnoreCloseOutputStream(OutputStream outputStream) {
    super(outputStream);
  }

  @Override
  public void close() {
    // ignore
  }
}
