/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.trace.ReadableSpan;
import java.util.function.Predicate;

/** Builder class for {@link SimpleSpanProcessor}. */
public final class SimpleSpanProcessorBuilder {
  private final SpanExporter spanExporter;
  private Predicate<ReadableSpan> exportPredicate = span -> span.getSpanContext().isSampled();

  SimpleSpanProcessorBuilder(SpanExporter spanExporter) {
    this.spanExporter = requireNonNull(spanExporter, "spanExporter");
  }

  /**
   * Sets a {@link Predicate Predicate&lt;ReadableSpan&gt;} that filters the {@link ReadableSpan}s
   * that are to be exported. If unset, defaults to exporting sampled spans.
   */
  public SimpleSpanProcessorBuilder setExportPredicate(Predicate<ReadableSpan> exportPredicate) {
    this.exportPredicate = requireNonNull(exportPredicate, "exportPredicate");
    return this;
  }

  /**
   * Returns a new {@link SimpleSpanProcessor} that converts spans to proto and forwards them to the
   * given {@code spanExporter}.
   *
   * @return a new {@link SimpleSpanProcessor}.
   */
  public SimpleSpanProcessor build() {
    return new SimpleSpanProcessor(spanExporter, exportPredicate);
  }
}
