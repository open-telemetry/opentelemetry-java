/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.traces.ResourceSpansMarshaler;
import io.opentelemetry.exporter.internal.otlp.traces.SpanReusableDataMarshaler;
import io.opentelemetry.exporter.logging.otlp.internal.InternalBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.trace.OtlpJsonLoggingSpanExporterBuilder;
import io.opentelemetry.exporter.logging.otlp.internal.trace.SpanBuilderAccessUtil;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A {@link SpanExporter} which writes {@linkplain SpanData spans} to a {@link Logger} in OTLP JSON
 * format. Each log line will include a single {@code ResourceSpans}.
 */
public final class OtlpJsonLoggingSpanExporter implements SpanExporter {

  private static final Logger logger =
      Logger.getLogger(OtlpJsonLoggingSpanExporter.class.getName());

  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final JsonWriter jsonWriter;

  private final Function<Collection<SpanData>, CompletableResultCode> marshaler;
  private final MemoryMode memoryMode;
  private final boolean wrapperJsonObject;

  static {
    SpanBuilderAccessUtil.setToExporter(
        builder ->
            new OtlpJsonLoggingSpanExporter(
                builder.getJsonWriter(), builder.getMemoryMode(), builder.isWrapperJsonObject()));
    SpanBuilderAccessUtil.setToBuilder(
        exporter ->
            InternalBuilder.forSpans()
                .setJsonWriter(exporter.jsonWriter)
                .setWrapperJsonObject(exporter.wrapperJsonObject)
                .setMemoryMode(exporter.memoryMode));
  }

  /** Returns a new {@link OtlpJsonLoggingSpanExporter}. */
  public static SpanExporter create() {
    return OtlpJsonLoggingSpanExporterBuilder.create().build();
  }

  OtlpJsonLoggingSpanExporter(
      JsonWriter jsonWriter, MemoryMode memoryMode, boolean wrapperJsonObject) {
    this.memoryMode = memoryMode;
    this.wrapperJsonObject = wrapperJsonObject;
    this.jsonWriter = jsonWriter;

    marshaler = createMarshaler(jsonWriter, memoryMode, wrapperJsonObject);
  }

  private static Function<Collection<SpanData>, CompletableResultCode> createMarshaler(
      JsonWriter jsonWriter, MemoryMode memoryMode, boolean wrapperJsonObject) {

    if (wrapperJsonObject) {
      SpanReusableDataMarshaler reusableDataMarshaler =
          new SpanReusableDataMarshaler(memoryMode) {
            @Override
            public CompletableResultCode doExport(Marshaler exportRequest, int numItems) {
              return jsonWriter.write(exportRequest);
            }
          };

      return reusableDataMarshaler::export;
    } else {
      return spans -> {
        // not support for low allocation marshaler

        for (ResourceSpansMarshaler resourceSpans : ResourceSpansMarshaler.create(spans)) {
          CompletableResultCode resultCode = jsonWriter.write(resourceSpans);
          if (!resultCode.isSuccess()) {
            // already logged
            return resultCode;
          }
        }
        return CompletableResultCode.ofSuccess();
      };
    }
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    if (isShutdown.get()) {
      return CompletableResultCode.ofFailure();
    }

    return marshaler.apply(spans);
  }

  @Override
  public CompletableResultCode flush() {
    return jsonWriter.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    if (!isShutdown.compareAndSet(false, true)) {
      logger.log(Level.INFO, "Calling shutdown() multiple times.");
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpJsonLoggingSpanExporter{", "}");
    joiner.add("memoryMode=" + memoryMode);
    joiner.add("wrapperJsonObject=" + wrapperJsonObject);
    joiner.add("jsonWriter=" + jsonWriter);
    return joiner.toString();
  }
}
