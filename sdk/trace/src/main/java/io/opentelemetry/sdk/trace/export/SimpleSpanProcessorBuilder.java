/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.export;

import static java.util.Objects.requireNonNull;

/** Builder class for {@link SimpleSpanProcessor}. */
public final class SimpleSpanProcessorBuilder {
  private final SpanExporter spanExporter;
  private boolean exportUnsampledSpans = false;

  SimpleSpanProcessorBuilder(SpanExporter spanExporter) {
    this.spanExporter = requireNonNull(spanExporter, "spanExporter");
  }

  /**
   * Sets whether unsampled spans should be exported. If unset, unsampled spans will not be
   * exported.
   */
  public SimpleSpanProcessorBuilder exportUnsampledSpans(boolean exportUnsampledSpans) {
    this.exportUnsampledSpans = exportUnsampledSpans;
    return this;
  }

  /**
   * Returns a new {@link SimpleSpanProcessor} that converts spans to proto and forwards them to the
   * given {@code spanExporter}.
   *
   * @return a new {@link SimpleSpanProcessor}.
   */
  public SimpleSpanProcessor build() {
    return new SimpleSpanProcessor(spanExporter, exportUnsampledSpans);
  }
}
