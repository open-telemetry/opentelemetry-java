/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal.logs;

import io.opentelemetry.exporter.logging.otlp.OtlpJsonLoggingLogRecordExporter;
import io.opentelemetry.exporter.logging.otlp.internal.AccessUtil;
import io.opentelemetry.exporter.logging.otlp.internal.writer.JsonWriter;
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

  public static class Argument {
    JsonWriter jsonWriter;
    boolean wrapperJsonObject;

    public Argument(JsonWriter jsonWriter, boolean wrapperJsonObject) {
      this.jsonWriter = jsonWriter;
      this.wrapperJsonObject = wrapperJsonObject;
    }

    public JsonWriter getJsonWriter() {
      return jsonWriter;
    }

    public boolean isWrapperJsonObject() {
      return wrapperJsonObject;
    }
  }

  private LogRecordBuilderAccessUtil() {}

  private static Function<Argument, OtlpJsonLoggingLogRecordExporter> toExporter;
  private static Function<OtlpJsonLoggingLogRecordExporter, Argument> toBuilder;

  public static Function<Argument, OtlpJsonLoggingLogRecordExporter> getToExporter() {
    return toExporter;
  }

  public static void setToExporter(
      Function<Argument, OtlpJsonLoggingLogRecordExporter> toExporter) {
    LogRecordBuilderAccessUtil.toExporter = toExporter;
  }

  public static Function<OtlpJsonLoggingLogRecordExporter, Argument> getToBuilder() {
    return toBuilder;
  }

  public static void setToBuilder(Function<OtlpJsonLoggingLogRecordExporter, Argument> toBuilder) {
    LogRecordBuilderAccessUtil.toBuilder = toBuilder;
  }
}
