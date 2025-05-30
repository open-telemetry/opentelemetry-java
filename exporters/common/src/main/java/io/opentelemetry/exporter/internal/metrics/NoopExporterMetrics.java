/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.metrics;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;

class NoopExporterMetrics implements ExporterMetrics {

  static final NoopExporterMetrics INSTANCE = new NoopExporterMetrics();

  @Override
  public Recording startRecordingExport(int itemCount) {
    return new NoopRecording();
  }

  private static class NoopRecording extends Recording {

    @Override
    protected void doFinish(@Nullable String errorType, Attributes requestAttributes) {}
  }
}
