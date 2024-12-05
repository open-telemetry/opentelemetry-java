/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.logging.otlp.internal.traces.OtlpStdoutSpanExporter;
import io.opentelemetry.exporter.logging.otlp.internal.traces.OtlpStdoutSpanExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.logging.Logger;

/**
 * A {@link SpanExporter} which writes {@linkplain SpanData spans} to a {@link Logger} in OTLP JSON
 * format. Each log line will include a single {@code ResourceSpans}.
 */
public final class OtlpJsonLoggingSpanExporter implements SpanExporter {

  private static final Logger logger =
      Logger.getLogger(OtlpJsonLoggingSpanExporter.class.getName());

  private final OtlpStdoutSpanExporter delegate;

  /** Returns a new {@link OtlpJsonLoggingSpanExporter}. */
  public static SpanExporter create() {
    OtlpStdoutSpanExporter delegate =
        new OtlpStdoutSpanExporterBuilder(logger).setWrapperJsonObject(false).build();
    return new OtlpJsonLoggingSpanExporter(delegate);
  }

  OtlpJsonLoggingSpanExporter(OtlpStdoutSpanExporter delegate) {
    this.delegate = delegate;
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> logs) {
    return delegate.export(logs);
  }

  @Override
  public CompletableResultCode flush() {
    return delegate.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }
}
