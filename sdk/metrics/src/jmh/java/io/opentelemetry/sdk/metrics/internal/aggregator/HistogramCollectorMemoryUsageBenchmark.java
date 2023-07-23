package io.opentelemetry.sdk.metrics.internal.aggregator;

public final class HistogramCollectorMemoryUsageBenchmark {

  private HistogramCollectorMemoryUsageBenchmark() {
  }

  public static void main(String[] args) {
    HistogramCollectBenchmark histogramCollectBenchmark = new HistogramCollectBenchmark();
    histogramCollectBenchmark.measureMemoryUsage();
  }
}
