/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil;
import io.opentelemetry.sdk.common.export.MemoryMode;

final class OtlpGrpcLogUtil {

  private OtlpGrpcLogUtil() {}

  /** See {@link OtlpConfigUtil#setMemoryModeOnOtlpExporterBuilder(Object, MemoryMode)}. */
  static void setMemoryMode(OtlpGrpcLogRecordExporterBuilder builder, MemoryMode memoryMode) {
    builder.setMemoryMode(memoryMode);
  }
}
