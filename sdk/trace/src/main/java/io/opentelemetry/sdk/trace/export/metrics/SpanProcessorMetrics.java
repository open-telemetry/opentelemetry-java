package io.opentelemetry.sdk.trace.export.metrics;

public interface SpanProcessorMetrics extends AutoCloseable {

  static SpanProcessorMetrics noop() {
    return new NoopSpanProcessorMetrics();
  }

  void recordSpanProcessedSuccessfully();

  void recordSpanProcessingFailed(String errorType);

  @Override
  void close();

}
