package io.opentelemetry.sdk.metrics.internal.aggregator.prototype;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;

public class NoopMetricExporter implements MetricExporter {
  private final AggregationTemporality aggregationTemporality;
  private final Aggregation aggregation;
  private int pointSoFar = 0;
  private final MemoryMode memoryMode;

  public NoopMetricExporter(
      AggregationTemporality aggregationTemporality,
      Aggregation aggregation,
      MemoryMode memoryMode) {
    this.aggregationTemporality = aggregationTemporality;
    this.aggregation = aggregation;
    this.memoryMode = memoryMode;
  }

  @SuppressWarnings("SystemOut")
  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    // Printing to make sure JVM won't optimize this out
    pointSoFar += metrics.size();

    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    if (pointSoFar >= 0) {
      return CompletableResultCode.ofSuccess();
    } else {
      return CompletableResultCode.ofSuccess();
    }
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return aggregation;
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return aggregationTemporality;
  }

  @Override
  public MemoryMode getMemoryMode() {
    return memoryMode;
  }
}
