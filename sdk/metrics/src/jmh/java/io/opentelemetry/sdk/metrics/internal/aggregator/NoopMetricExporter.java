package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MemoryMode;
import io.opentelemetry.sdk.metrics.export.MetricExporter;

import java.util.Collection;

import static io.opentelemetry.sdk.metrics.export.MemoryMode.IMMUTABLE_DATA;

class NoopMetricExporter implements MetricExporter {
  private final AggregationTemporality aggregationTemporality;
  private final Aggregation aggregation;
  private final MemoryMode memoryMode;

  NoopMetricExporter(
      AggregationTemporality aggregationTemporality, Aggregation aggregation) {
    this(aggregationTemporality, aggregation, IMMUTABLE_DATA);
  }

  NoopMetricExporter(
      AggregationTemporality aggregationTemporality, Aggregation aggregation, MemoryMode memoryMode) {
    this.aggregationTemporality = aggregationTemporality;
    this.aggregation = aggregation;
    this.memoryMode = memoryMode;
  }

  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    return CompletableResultCode.ofSuccess();
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
