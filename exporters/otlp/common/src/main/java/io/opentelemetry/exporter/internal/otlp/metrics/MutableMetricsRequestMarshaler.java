package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.otlp.MarshallerObjectPools;
import io.opentelemetry.sdk.internal.DynamicList;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import java.util.List;

public class MutableMetricsRequestMarshaler extends MetricsRequestMarshaler {
  private DynamicList<ResourceMetricsMarshaler> resourceMetricsMarshalers = DynamicList.empty();
  private int size;

    /**
    * Returns a {@link MetricsRequestMarshaler} that can be used to convert the provided {@link
    * MetricData} into a serialized OTLP ExportMetricsServiceRequest.
    */
    public static MetricsRequestMarshaler create(
        Collection<MetricData> metricDataList,
        MarshallerObjectPools marshallerObjectPools) {

      MutableMetricsRequestMarshaler mutableMetricsRequestMarshaler =
          marshallerObjectPools
              .getMutableMetricsRequestMarshallerPool()
              .borrowObject();

      DynamicList<ResourceMetricsMarshaler> dynamicList =
          mutableMetricsRequestMarshaler.resourceMetricsMarshalers;

      MutableResourceMetricsMarshaler.createIntoDynamicList(
          metricDataList,
          dynamicList,
          marshallerObjectPools);

      mutableMetricsRequestMarshaler.set(dynamicList);

      return mutableMetricsRequestMarshaler;
    }

  @Override
  List<ResourceMetricsMarshaler> getResourceMetricsMarshalers() {
    return resourceMetricsMarshalers;
  }

  private void set(DynamicList<ResourceMetricsMarshaler> resourceMetrics) {
    this.resourceMetricsMarshalers = resourceMetrics;
    this.size = calculateSize(resourceMetrics);
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }
}
