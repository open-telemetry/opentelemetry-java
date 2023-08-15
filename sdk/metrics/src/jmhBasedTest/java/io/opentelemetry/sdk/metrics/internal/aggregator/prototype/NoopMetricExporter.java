package io.opentelemetry.sdk.metrics.internal.aggregator.prototype;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;

public class NoopMetricExporter implements MetricExporter {
  private final AggregationTemporality aggregationTemporality;
  private final Aggregation aggregation;

  public NoopMetricExporter(
      AggregationTemporality aggregationTemporality, Aggregation aggregation) {
    this.aggregationTemporality = aggregationTemporality;
    this.aggregation = aggregation;
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
}
