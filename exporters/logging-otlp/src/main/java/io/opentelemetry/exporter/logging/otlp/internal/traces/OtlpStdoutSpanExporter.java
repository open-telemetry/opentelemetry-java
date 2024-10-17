/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.traces;

import io.opentelemetry.exporter.internal.otlp.traces.ResourceSpansMarshaler;
import io.opentelemetry.exporter.internal.otlp.traces.SpanReusableDataMarshaler;
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
 * Exporter for sending OTLP spans to stdout.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpStdoutSpanExporter implements SpanExporter {

  private static final Logger LOGGER = Logger.getLogger(OtlpStdoutSpanExporter.class.getName());

  private final AtomicBoolean isShutdown = new AtomicBoolean();

  private final Logger logger;
  private final JsonWriter jsonWriter;
  private final boolean wrapperJsonObject;
  private final MemoryMode memoryMode;
  private final Function<Collection<SpanData>, CompletableResultCode> marshaler;

  OtlpStdoutSpanExporter(
      Logger logger, JsonWriter jsonWriter, boolean wrapperJsonObject, MemoryMode memoryMode) {
    this.logger = logger;
    this.jsonWriter = jsonWriter;
    this.wrapperJsonObject = wrapperJsonObject;
    this.memoryMode = memoryMode;
    marshaler = createMarshaler(jsonWriter, memoryMode, wrapperJsonObject);
  }

  /** Returns a new {@link OtlpStdoutSpanExporterBuilder}. */
  @SuppressWarnings("SystemOut")
  public static OtlpStdoutSpanExporterBuilder builder() {
    return new OtlpStdoutSpanExporterBuilder(LOGGER).setOutput(System.out);
  }

  private static Function<Collection<SpanData>, CompletableResultCode> createMarshaler(
      JsonWriter jsonWriter, MemoryMode memoryMode, boolean wrapperJsonObject) {
    if (wrapperJsonObject) {
      SpanReusableDataMarshaler reusableDataMarshaler =
          new SpanReusableDataMarshaler(
              memoryMode, (marshaler, numItems) -> jsonWriter.write(marshaler));
      return reusableDataMarshaler::export;
    } else {
      return spans -> {
        // no support for low allocation marshaler
        for (ResourceSpansMarshaler marshaler : ResourceSpansMarshaler.create(spans)) {
          CompletableResultCode resultCode = jsonWriter.write(marshaler);
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
    } else {
      jsonWriter.close();
    }
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpStdoutSpanExporter{", "}");
    joiner.add("jsonWriter=" + jsonWriter);
    joiner.add("wrapperJsonObject=" + wrapperJsonObject);
    joiner.add("memoryMode=" + memoryMode);
    return joiner.toString();
  }
}
