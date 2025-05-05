package io.opentelemetry.exporter.internal.metrics;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.Nullable;

class NoopExporterMetrics implements ExporterMetrics {

  static final NoopExporterMetrics INSTANCE = new NoopExporterMetrics();

  @Override
  public Recording startRecordingExport(int itemCount) {
    return NoopRecording.INSTANCE;
  }

  private static class NoopRecording extends Recording {

    private static final NoopRecording INSTANCE = new NoopRecording();

    @Override
    protected void doFinish(@Nullable String errorType, Attributes requestAttributes) { }
  }
}
