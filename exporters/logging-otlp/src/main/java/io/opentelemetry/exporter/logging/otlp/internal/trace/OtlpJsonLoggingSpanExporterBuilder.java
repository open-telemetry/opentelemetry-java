/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.trace;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.internal.InternalBuilder;
import io.opentelemetry.sdk.common.export.MemoryMode;
import java.io.OutputStream;

/**
 * Builder for {@link OtlpJsonLoggingSpanExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OtlpJsonLoggingSpanExporterBuilder {

  private final InternalBuilder builder;

  OtlpJsonLoggingSpanExporterBuilder(InternalBuilder builder) {
    this.builder = builder;
  }

  /**
   * Creates a new {@link OtlpJsonLoggingSpanExporterBuilder} with default settings.
   *
   * @return a new {@link OtlpJsonLoggingSpanExporterBuilder}.
   */
  public static OtlpJsonLoggingSpanExporterBuilder create() {
    return new OtlpJsonLoggingSpanExporterBuilder(InternalBuilder.forSpans());
  }

  /**
   * Creates a new {@link OtlpJsonLoggingSpanExporterBuilder} from an existing exporter.
   *
   * @param exporter the existing exporter.
   * @return a new {@link OtlpJsonLoggingSpanExporterBuilder}.
   */
  public static OtlpJsonLoggingSpanExporterBuilder createFromExporter(
      OtlpJsonLoggingSpanExporter exporter) {
    return new OtlpJsonLoggingSpanExporterBuilder(
        SpanBuilderAccessUtil.getToBuilder().apply(exporter));
  }

  /**
   * Sets the exporter to use the specified JSON object wrapper.
   *
   * @param wrapperJsonObject whether to wrap the JSON object in an outer JSON "resourceLogs"
   *     object.
   */
  public OtlpJsonLoggingSpanExporterBuilder setWrapperJsonObject(boolean wrapperJsonObject) {
    builder.setWrapperJsonObject(wrapperJsonObject);
    return this;
  }

  /**
   * Set the {@link MemoryMode}. If unset, defaults to {@link MemoryMode#IMMUTABLE_DATA}.
   *
   * <p>When memory mode is {@link MemoryMode#REUSABLE_DATA}, serialization is optimized to reduce
   * memory allocation.
   */
  public OtlpJsonLoggingSpanExporterBuilder setMemoryMode(MemoryMode memoryMode) {
    builder.setMemoryMode(memoryMode);
    return this;
  }

  /** Sets the exporter to use the logger for output. */
  public OtlpJsonLoggingSpanExporterBuilder setUseLogger() {
    builder.setUseLogger();
    return this;
  }

  /**
   * Sets the exporter to use the specified output stream.
   *
   * @param outputStream the output stream to use.
   */
  public OtlpJsonLoggingSpanExporterBuilder setOutputStream(OutputStream outputStream) {
    builder.setOutputStream(outputStream);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpJsonLoggingSpanExporter build() {
    return SpanBuilderAccessUtil.getToExporter().apply(builder);
  }
}
