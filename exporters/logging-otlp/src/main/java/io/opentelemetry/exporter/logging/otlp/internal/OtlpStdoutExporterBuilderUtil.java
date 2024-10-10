/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.logging.otlp.internal;

import io.opentelemetry.sdk.common.export.MemoryMode;

public class OtlpStdoutExporterBuilderUtil {
  public OtlpStdoutExporterBuilderUtil() {}

  public static void validate(MemoryMode memoryMode, boolean wrapperJsonObject) {
    if (memoryMode == MemoryMode.REUSABLE_DATA && !wrapperJsonObject) {
      throw new IllegalArgumentException(
          "Reusable data mode is not supported without wrapperJsonObject");
    }
  }
}
