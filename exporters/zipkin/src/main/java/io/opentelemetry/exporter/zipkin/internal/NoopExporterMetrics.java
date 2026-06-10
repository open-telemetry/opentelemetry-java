/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;

/**
 * Copied from {@code io.opentelemetry.exporter.internal.NoopExporterMetrics} to avoid shared
 * internal code.
 */
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
