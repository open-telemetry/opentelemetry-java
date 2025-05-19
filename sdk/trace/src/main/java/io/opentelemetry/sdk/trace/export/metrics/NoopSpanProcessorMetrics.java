package io.opentelemetry.sdk.trace.export.metrics;

class NoopSpanProcessorMetrics implements SpanProcessorMetrics {

  static final NoopSpanProcessorMetrics INSTANCE = new NoopSpanProcessorMetrics();

  @Override
  public void recordSpanProcessedSuccessfully() {
  }

  @Override
  public void recordSpanProcessingFailed(String errorType) {
  }
}
