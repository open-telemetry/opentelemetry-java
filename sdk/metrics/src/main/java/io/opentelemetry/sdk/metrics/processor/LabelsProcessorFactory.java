package io.opentelemetry.sdk.metrics.processor;

public interface LabelsProcessorFactory {
  static LabelsProcessorFactory noop() {
    return NoopLabelsProcessor::new;
  }
  static LabelsProcessorFactory baggageExtractor(BaggageMetricsLabelsExtractor labelsExtractor) {
    return () -> new BaggageLabelsProcessor(labelsExtractor);
  }

  /**
   * Returns a new {@link LabelsProcessorFactory}
   *
   * @return new {@link LabelsProcessorFactory}
   */
  LabelsProcessor create();
}
