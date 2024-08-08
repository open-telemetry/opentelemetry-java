/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.stream;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.OtlpExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StreamExporter<T extends Marshaler> implements OtlpExporter<T> {

  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private static final Logger internalLogger = Logger.getLogger(StreamExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final OutputStream outputStream;

  public StreamExporter(OutputStream outputStream) {
    this.outputStream = outputStream;
  }

  @Override
  public CompletableResultCode export(T exportRequest, int numItems) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }
    try {
      exportRequest.writeJsonTo(outputStream);
      outputStream.flush();
      return CompletableResultCode.ofSuccess();
    } catch (IOException e) {
      logger.log(Level.WARNING, "Failed to export items", e);
      return CompletableResultCode.ofFailure();
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
    }
    return CompletableResultCode.ofSuccess();
  }
}
