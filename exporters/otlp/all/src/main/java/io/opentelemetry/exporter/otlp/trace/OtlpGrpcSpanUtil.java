/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil;
import io.opentelemetry.sdk.common.export.MemoryMode;

final class OtlpGrpcSpanUtil {

  private OtlpGrpcSpanUtil() {}

  /** See {@link OtlpConfigUtil#setMemoryModeOnOtlpExporterBuilder(Object, MemoryMode)}. */
  static void setMemoryMode(OtlpGrpcSpanExporterBuilder builder, MemoryMode memoryMode) {
    builder.setMemoryMode(memoryMode);
  }
}
