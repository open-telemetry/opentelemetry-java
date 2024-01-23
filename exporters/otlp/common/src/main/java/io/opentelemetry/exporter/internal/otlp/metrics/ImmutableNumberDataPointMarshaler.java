package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.otlp.ImmutableKeyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.NumberDataPoint;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.PointData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public final class ImmutableNumberDataPointMarshaler extends NumberDataPointMarshaler {
  private final long startTimeUnixNano;
  private final long timeUnixNano;

  private final PointData value;

  private final ProtoFieldInfo valueField;

  private final List<ExemplarMarshaler> exemplars;

  private final List<KeyValueMarshaler> attributes;

  private final int size;

  static List<NumberDataPointMarshaler> createRepeated(Collection<? extends PointData> points) {
    int numPoints = points.size();
    List<NumberDataPointMarshaler> marshalers = new ArrayList<>(numPoints);
    for (PointData point : points) {
      marshalers.add(ImmutableNumberDataPointMarshaler.create(point));
    }
    return marshalers;
  }

  static NumberDataPointMarshaler create(PointData point) {
    List<ExemplarMarshaler> exemplarMarshalers = ImmutableExemplarMarshaler.createRepeated(
        point.getExemplars());
    List<KeyValueMarshaler> attributeMarshalers =
        ImmutableKeyValueMarshaler.createForAttributes(point.getAttributes());

    ProtoFieldInfo valueField;
    if (point instanceof LongPointData) {
      valueField = NumberDataPoint.AS_INT;
    } else {
      assert point instanceof DoublePointData;
      valueField = NumberDataPoint.AS_DOUBLE;
    }

    return new ImmutableNumberDataPointMarshaler(
        point.getStartEpochNanos(),
        point.getEpochNanos(),
        point,
        valueField,
        exemplarMarshalers,
        attributeMarshalers);
  }

  private ImmutableNumberDataPointMarshaler(
      long startTimeUnixNano,
      long timeUnixNano,
      PointData value,
      ProtoFieldInfo valueField,
      List<ExemplarMarshaler> exemplars,
      List<KeyValueMarshaler> attributes) {
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
  protected long getStartTimeUnixNano() {
    return startTimeUnixNano;
  }

  @Override
  protected long getTimeUnixNano() {
    return timeUnixNano;
  }

  @Override
  protected PointData getValue() {
    return value;
  }

  @Override
  protected ProtoFieldInfo getValueField() {
    return valueField;
  }

  @Override
  protected List<ExemplarMarshaler> getExemplars() {
    return exemplars;
  }

  @Override
  protected List<KeyValueMarshaler> getAttributes() {
    return attributes;
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }
}
