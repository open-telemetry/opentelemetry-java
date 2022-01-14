/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

/** A Span Exporter that logs every span at INFO level using java.util.logging. */
public final class LoggingSpanExporter implements SpanExporter {
  private static final Logger logger = Logger.getLogger(LoggingSpanExporter.class.getName());

  /** Returns a new {@link LoggingSpanExporter}. */
  public static LoggingSpanExporter create() {
    return new LoggingSpanExporter();
  }

  /**
   * Class constructor.
   *
   * @deprecated Use {@link #create()}.
   */
  @Deprecated
  public LoggingSpanExporter() {}

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    // We always have 32 + 16 + name + several whitespace, 60 seems like an OK initial guess.
    StringBuilder sb = new StringBuilder(60);
    for (SpanData span : spans) {
      sb.setLength(0);
      InstrumentationLibraryInfo instrumentationLibraryInfo = span.getInstrumentationLibraryInfo();
      sb.append("'")
          .append(span.getName())
          .append("' : ")
          .append(span.getTraceId())
          .append(" ")
          .append(span.getSpanId())
          .append(" ")
          .append(span.getKind())
          .append(" [tracer: ")
          .append(instrumentationLibraryInfo.getName())
          .append(":")
          .append(
              instrumentationLibraryInfo.getVersion() == null
                  ? ""
                  : instrumentationLibraryInfo.getVersion())
          .append("] ")
          .append(span.getAttributes());
      logger.log(Level.INFO, sb.toString());
    }
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Flushes the data.
   *
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode flush() {
    CompletableResultCode resultCode = new CompletableResultCode();
    for (Handler handler : logger.getHandlers()) {
      try {
        handler.flush();
      } catch (Throwable t) {
        resultCode.fail();
      }
    }
    return resultCode.succeed();
  }

  @Override
  public CompletableResultCode shutdown() {
    return flush();
  }
}
