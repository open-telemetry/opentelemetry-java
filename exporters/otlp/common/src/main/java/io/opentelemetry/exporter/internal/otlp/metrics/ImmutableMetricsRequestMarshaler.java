package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import java.util.List;

public class ImmutableMetricsRequestMarshaler extends MetricsRequestMarshaler {
  private final List<ResourceMetricsMarshaler> resourceMetricsMarshalers;
  private final int size;

  /**
   * Returns a {@link MetricsRequestMarshaler} that can be used to convert the provided {@link
   * MetricData} into a serialized OTLP ExportMetricsServiceRequest.
   */
  public static MetricsRequestMarshaler create(Collection<MetricData> metricDataList) {
    return new ImmutableMetricsRequestMarshaler(
        ImmutableResourceMetricsMarshaler.create(metricDataList));
  }

  protected ImmutableMetricsRequestMarshaler(
      List<ResourceMetricsMarshaler> resourceMetricsMarshalers) {
    this.resourceMetricsMarshalers = resourceMetricsMarshalers;
    this.size = calculateSize(resourceMetricsMarshalers);
  }


  @Override
  public int getBinarySerializedSize() {
    return size;
  }

  @Override
  List<ResourceMetricsMarshaler> getResourceMetricsMarshalers() {
    return resourceMetricsMarshalers;
  }
}
