/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.metrics;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingMetricExporter;
import io.opentelemetry.exporter.logging.otlp.internal.AccessUtil;
import java.util.function.Function;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@SuppressWarnings({"NonFinalStaticField", "NullAway"})
public class MetricBuilderAccessUtil {

  static {
    AccessUtil.ensureLoaded(OtlpJsonLoggingMetricExporter.class);
  }

  private MetricBuilderAccessUtil() {}

  private static Function<InternalMetricBuilder, OtlpJsonLoggingMetricExporter> toExporter;
  private static Function<OtlpJsonLoggingMetricExporter, InternalMetricBuilder> toBuilder;

  public static Function<InternalMetricBuilder, OtlpJsonLoggingMetricExporter> getToExporter() {
    return toExporter;
  }

  public static void setToExporter(
      Function<InternalMetricBuilder, OtlpJsonLoggingMetricExporter> toExporter) {
    MetricBuilderAccessUtil.toExporter = toExporter;
  }

  public static Function<OtlpJsonLoggingMetricExporter, InternalMetricBuilder> getToBuilder() {
    return toBuilder;
  }

  public static void setToBuilder(
      Function<OtlpJsonLoggingMetricExporter, InternalMetricBuilder> toBuilder) {
    MetricBuilderAccessUtil.toBuilder = toBuilder;
  }
}
