/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import io.opentelemetry.exporter.otlp.internal.OtlpConfigUtil;
import io.opentelemetry.sdk.common.export.MemoryMode;

final class OtlpGrpcMetricUtil {

  private OtlpGrpcMetricUtil() {}

  /** See {@link OtlpConfigUtil#setMemoryModeOnOtlpExporterBuilder(Object, MemoryMode)}. */
  static void setMemoryMode(OtlpGrpcMetricExporterBuilder builder, MemoryMode memoryMode) {
    builder.setMemoryMode(memoryMode);
  }
}
