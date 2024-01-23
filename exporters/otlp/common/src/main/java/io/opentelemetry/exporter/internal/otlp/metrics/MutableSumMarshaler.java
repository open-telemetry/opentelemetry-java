package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.otlp.MarshallerObjectPools;
import io.opentelemetry.sdk.internal.DynamicList;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import java.util.List;

public class MutableSumMarshaler extends SumMarshaler {
  private DynamicList<NumberDataPointMarshaler> dataPoints;
  private ProtoEnumInfo aggregationTemporality;
  private boolean isMonotonic;
  private int size;

  static SumMarshaler create(
      SumData<? extends PointData> sum,
      MarshallerObjectPools marshallerObjectPools) {
    DynamicList<NumberDataPointMarshaler> dataPointMarshalers =
        MutableNumberDataPointMarshaler.createRepeated(sum.getPoints(), marshallerObjectPools);

    MutableSumMarshaler mutableSumMarshaler = marshallerObjectPools
        .getMutableSumMarshalerPool()
        .borrowObject();

    mutableSumMarshaler.set(
        dataPointMarshalers,
        MetricsMarshalerUtil.mapToTemporality(sum.getAggregationTemporality()),
        sum.isMonotonic());
    return mutableSumMarshaler;
  }

  void set(
      DynamicList<NumberDataPointMarshaler> dataPoints,
      ProtoEnumInfo aggregationTemporality,
      boolean isMonotonic) {
    this.dataPoints = dataPoints;
    this.aggregationTemporality = aggregationTemporality;
    this.isMonotonic = isMonotonic;
    this.size = calculateSize(dataPoints, aggregationTemporality, isMonotonic);
  }
}
