/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.stream.trace;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.stream.StreamExporterBuilder;
import io.opentelemetry.sdk.common.export.MemoryMode;
import java.io.OutputStream;

/**
 * Builder for {@link StdoutSpanExporter}.
 *
 * @since 1.27.0
 */
public final class StdoutSpanExporterBuilder {

  private static final MemoryMode DEFAULT_MEMORY_MODE = MemoryMode.IMMUTABLE_DATA;

  // Visible for testing
  final StreamExporterBuilder<Marshaler> delegate;
  private MemoryMode memoryMode;

  StdoutSpanExporterBuilder(StreamExporterBuilder<Marshaler> delegate, MemoryMode memoryMode) {
    this.delegate = delegate;
    this.memoryMode = memoryMode;
  }

  StdoutSpanExporterBuilder() {
    this(new StreamExporterBuilder<>(), DEFAULT_MEMORY_MODE);
  }

  /**
   * Set the {@link MemoryMode}. If unset, defaults to {@link #DEFAULT_MEMORY_MODE}.
   *
   * <p>When memory mode is {@link MemoryMode#REUSABLE_DATA}, serialization is optimized to reduce
   * memory allocation.
   */
  public StdoutSpanExporterBuilder setMemoryMode(MemoryMode memoryMode) {
    requireNonNull(memoryMode, "memoryMode");
    this.memoryMode = memoryMode;
    return this;
  }

  public StdoutSpanExporterBuilder setOutputStream(OutputStream outputStream) {
    requireNonNull(outputStream, "outputStream");
    this.delegate.setOutputStream(outputStream);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public StdoutSpanExporter build() {
    return new StdoutSpanExporter(delegate, delegate.build(), memoryMode);
  }
}
