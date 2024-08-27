/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.AccessUtil;
import io.opentelemetry.exporter.logging.otlp.internal.InternalBuilder;
import java.util.function.Function;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@SuppressWarnings({"NonFinalStaticField", "NullAway"})
public class LogRecordBuilderAccessUtil {

  static {
    AccessUtil.ensureLoaded(OtlpJsonLoggingLogRecordExporter.class);
  }

  private LogRecordBuilderAccessUtil() {}

  private static Function<InternalBuilder, OtlpJsonLoggingLogRecordExporter> toExporter;
  private static Function<OtlpJsonLoggingLogRecordExporter, InternalBuilder> toBuilder;

  public static Function<InternalBuilder, OtlpJsonLoggingLogRecordExporter> getToExporter() {
    return toExporter;
  }

  public static void setToExporter(
      Function<InternalBuilder, OtlpJsonLoggingLogRecordExporter> toExporter) {
    LogRecordBuilderAccessUtil.toExporter = toExporter;
  }

  public static Function<OtlpJsonLoggingLogRecordExporter, InternalBuilder> getToBuilder() {
    return toBuilder;
  }

  public static void setToBuilder(
      Function<OtlpJsonLoggingLogRecordExporter, InternalBuilder> toBuilder) {
    LogRecordBuilderAccessUtil.toBuilder = toBuilder;
  }
}
