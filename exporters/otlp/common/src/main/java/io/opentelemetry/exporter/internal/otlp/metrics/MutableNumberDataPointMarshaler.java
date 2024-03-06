package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.MarshallerObjectPools;
import io.opentelemetry.exporter.internal.otlp.MutableKeyValueMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.NumberDataPoint;
import io.opentelemetry.sdk.internal.DynamicList;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.PointData;
import javax.annotation.Nullable;
import java.util.Collection;

public final class MutableNumberDataPointMarshaler extends NumberDataPointMarshaler {
  private long startTimeUnixNano;
  private long timeUnixNano;

  @Nullable
  private PointData value;

  @Nullable
  private ProtoFieldInfo valueField;

  private DynamicList<ExemplarMarshaler> exemplars = DynamicList.empty();

  private DynamicList<KeyValueMarshaler> attributes = DynamicList.empty();

  private int size;

  static DynamicList<NumberDataPointMarshaler> createRepeated(
      Collection<? extends PointData> points,
      MarshallerObjectPools marshallerObjectPools) {
    int numPoints = points.size();
    DynamicList<NumberDataPointMarshaler> marshalers =
        marshallerObjectPools.borrowDynamicList(numPoints);
    for (PointData point : points) {
      marshalers.add(create(point, marshallerObjectPools));
    }
    return marshalers;
  }

  static NumberDataPointMarshaler create(
      PointData point,
      MarshallerObjectPools marshallerObjectPools) {

    MutableNumberDataPointMarshaler mutableNumberDataPointMarshaler =
        marshallerObjectPools.getMutableNumberDataPointMarshallerPool().borrowObject();

    DynamicList<ExemplarMarshaler> exemplarMarshalersDynamicList =
        mutableNumberDataPointMarshaler.getExemplars();
    MutableExemplarMarshaler.createRepeatedIntoDynamicList(
        point.getExemplars(),
        exemplarMarshalersDynamicList,
        marshallerObjectPools);

    DynamicList<KeyValueMarshaler> keyValueMarshalersDynamicList =
        mutableNumberDataPointMarshaler.getAttributes();
    MutableKeyValueMarshaler.createForAttributesIntoDynamicList(
        point.getAttributes(),
        keyValueMarshalersDynamicList,
        marshallerObjectPools);

    ProtoFieldInfo valueField;
    if (point instanceof LongPointData) {
      valueField = NumberDataPoint.AS_INT;
    } else {
      assert point instanceof DoublePointData;
      valueField = NumberDataPoint.AS_DOUBLE;
    }

    mutableNumberDataPointMarshaler.set(
        point.getStartEpochNanos(),
        point.getEpochNanos(),
        point,
        valueField,
        exemplarMarshalersDynamicList,
        keyValueMarshalersDynamicList);

    return mutableNumberDataPointMarshaler;
  }

  private void set(
      long startTimeUnixNano,
      long timeUnixNano,
      PointData value,
      ProtoFieldInfo valueField,
      DynamicList<ExemplarMarshaler> exemplars,
      DynamicList<KeyValueMarshaler> attributes) {
    this.startTimeUnixNano = startTimeUnixNano;
    this.timeUnixNano = timeUnixNano;
    this.value = value;
    this.valueField = valueField;
    this.exemplars = exemplars;
    this.attributes = attributes;
    this.size = calculateSize(startTimeUnixNano, timeUnixNano, valueField, value, exemplars,
        attributes);
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }

  @Override
  protected long getStartTimeUnixNano() {
    return startTimeUnixNano;
  }

  @Override
  protected long getTimeUnixNano() {
    return timeUnixNano;
  }

  @Nullable
  @Override
  protected PointData getValue() {
    return value;
  }

  @Nullable
  @Override
  protected ProtoFieldInfo getValueField() {
    return valueField;
  }

  @Override
  protected DynamicList<ExemplarMarshaler> getExemplars() {
    return exemplars;
  }

  @Override
  protected DynamicList<KeyValueMarshaler> getAttributes() {
    return attributes;
  }
}
