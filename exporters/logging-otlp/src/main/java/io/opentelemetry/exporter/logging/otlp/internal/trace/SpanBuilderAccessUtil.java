/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.trace;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingSpanExporter;
import io.opentelemetry.exporter.logging.otlp.internal.AccessUtil;
import io.opentelemetry.exporter.logging.otlp.internal.InternalBuilder;
import java.util.function.Function;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@SuppressWarnings({"NonFinalStaticField", "NullAway"})
public class SpanBuilderAccessUtil {

  static {
    AccessUtil.ensureLoaded(OtlpJsonLoggingSpanExporter.class);
  }

  private SpanBuilderAccessUtil() {}

  private static Function<InternalBuilder, OtlpJsonLoggingSpanExporter> toExporter;
  private static Function<OtlpJsonLoggingSpanExporter, InternalBuilder> toBuilder;

  public static Function<InternalBuilder, OtlpJsonLoggingSpanExporter> getToExporter() {
    return toExporter;
  }

  public static void setToExporter(
      Function<InternalBuilder, OtlpJsonLoggingSpanExporter> toExporter) {
    SpanBuilderAccessUtil.toExporter = toExporter;
  }

  public static Function<OtlpJsonLoggingSpanExporter, InternalBuilder> getToBuilder() {
    return toBuilder;
  }

  public static void setToBuilder(
      Function<OtlpJsonLoggingSpanExporter, InternalBuilder> toBuilder) {
    SpanBuilderAccessUtil.toBuilder = toBuilder;
  }
}
