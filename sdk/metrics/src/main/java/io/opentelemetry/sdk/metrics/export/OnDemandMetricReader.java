package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;

/**
 * A {@link MetricReader} implementation that only exports metrics when {@link #forceFlush()} is called.
 */
public final class OnDemandMetricReader implements MetricReader {
  private final MetricExporter exporter;
  private volatile CollectionRegistration collectionRegistration = CollectionRegistration.noop();

  public OnDemandMetricReader(MetricExporter exporter) {
    this.exporter = exporter;
  }

  @Override
  public void register(CollectionRegistration collectionRegistration) {
    this.collectionRegistration = collectionRegistration;
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return exporter.getAggregationTemporality(instrumentType);
  }

  @Override
  public Aggregation getDefaultAggregation(InstrumentType instrumentType) {
    return exporter.getDefaultAggregation(instrumentType);
  }

  @Override
  public MemoryMode getMemoryMode() {
    return exporter.getMemoryMode();
  }

  @Override
  public CompletableResultCode forceFlush() {
    try {
      Collection<MetricData> metricData = collectionRegistration.collectAllMetrics();
      if (metricData.isEmpty()) {
        return CompletableResultCode.ofSuccess();
      }
      return exporter.export(metricData);
    } catch (RuntimeException e) {
      return CompletableResultCode.ofExceptionalFailure(e);
    }
  }

  @Override
  public CompletableResultCode shutdown() {
    return exporter.shutdown();
  }

  @Override
  public String toString() {
    return "OnDemandMetricReader{exporter=" + exporter + "}";
  }
}
