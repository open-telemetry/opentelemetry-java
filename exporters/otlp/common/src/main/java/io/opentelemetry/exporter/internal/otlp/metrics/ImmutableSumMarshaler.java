package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import java.util.List;

public class ImmutableSumMarshaler extends SumMarshaler {
  private final List<NumberDataPointMarshaler> dataPoints;
  private final ProtoEnumInfo aggregationTemporality;
  private final boolean isMonotonic;
  private final int size;

  static SumMarshaler create(SumData<? extends PointData> sum) {
    List<NumberDataPointMarshaler> dataPointMarshalers =
        ImmutableNumberDataPointMarshaler.createRepeated(sum.getPoints());

    return new ImmutableSumMarshaler(
        dataPointMarshalers,
        MetricsMarshalerUtil.mapToTemporality(sum.getAggregationTemporality()),
        sum.isMonotonic());
  }

  protected ImmutableSumMarshaler(
      List<NumberDataPointMarshaler> dataPoints,
      ProtoEnumInfo aggregationTemporality,
      boolean isMonotonic) {
    this.dataPoints = dataPoints;
    this.aggregationTemporality = aggregationTemporality;
    this.isMonotonic = isMonotonic;
    this.size = calculateSize(dataPoints, aggregationTemporality, isMonotonic);
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }

  @Override
  List<NumberDataPointMarshaler> getDataPoints() {
    return dataPoints;
  }

  @Override
  ProtoEnumInfo getAggregationTemporality() {
    return aggregationTemporality;
  }

  @Override
  boolean getIsMonotonic() {
    return isMonotonic;
  }
}
