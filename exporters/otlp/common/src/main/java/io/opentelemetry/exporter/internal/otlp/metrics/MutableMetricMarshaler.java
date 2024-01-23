package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.otlp.MarshallerObjectPools;
import io.opentelemetry.proto.metrics.v1.internal.Metric;
import io.opentelemetry.sdk.metrics.data.MetricData;

public final class MutableMetricMarshaler extends MetricMarshaler {
  private String name;
  private String description;
  private String unit;
  private Marshaler dataMarshaler;
  private ProtoFieldInfo dataField;
  private int size;

  static Marshaler create(MetricData metric, MarshallerObjectPools marshallerObjectPools) {
    Marshaler dataMarshaler = null;
    ProtoFieldInfo dataField = null;
    switch (metric.getType()) {
      case LONG_GAUGE:
        // TODO Asaf: Change to mutable
        dataMarshaler = GaugeMarshaler.create(metric.getLongGaugeData());
        dataField = Metric.GAUGE;
        break;
      case DOUBLE_GAUGE:
        // TODO Asaf: Change to mutable
        dataMarshaler = GaugeMarshaler.create(metric.getDoubleGaugeData());
        dataField = Metric.GAUGE;
        break;
      case LONG_SUM:
        dataMarshaler = MutableSumMarshaler.create(metric.getLongSumData());
        dataField = Metric.SUM;
        break;
      case DOUBLE_SUM:
        dataMarshaler = MutableSumMarshaler.create(metric.getDoubleSumData());
        dataField = Metric.SUM;
        break;
      case SUMMARY:
        // TODO Asaf: Change to mutable
        dataMarshaler = SummaryMarshaler.create(metric.getSummaryData());
        dataField = Metric.SUMMARY;
        break;
      case HISTOGRAM:
        // TODO Asaf: Change to mutable
        dataMarshaler = HistogramMarshaler.create(metric.getHistogramData());
        dataField = Metric.HISTOGRAM;
        break;
      case EXPONENTIAL_HISTOGRAM:
        // TODO Asaf: Change to mutable
        dataMarshaler = ExponentialHistogramMarshaler.create(metric.getExponentialHistogramData());
        dataField = Metric.EXPONENTIAL_HISTOGRAM;
    }

    if (dataMarshaler == null || dataField == null) {
      // Someone not using BOM to align versions as we require. Just skip the metric.
      return NoopMarshaler.INSTANCE;
    }

    MutableMetricMarshaler mutableMetricMarshaler = marshallerObjectPools
        .getMutableMetricMarshalerPool()
        .borrowObject();

    mutableMetricMarshaler.set(
        metric.getName(),
        metric.getDescription(),
        metric.getUnit(),
        dataMarshaler,
        dataField);

    return mutableMetricMarshaler;
  }

  void set(
      String name,
      String description,
      String unit,
      Marshaler dataMarshaler,
      ProtoFieldInfo dataField) {
    this.name = name;
    this.description = description;
    this.unit = unit;
    this.dataMarshaler = dataMarshaler;
    this.dataField = dataField;
    this.size = calculateSize(name, description, unit, dataMarshaler, dataField);
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }

  @Override
  protected String getName() {
    return name;
  }

  @Override
  protected String getDescription() {
    return description;
  }

  @Override
  protected String getUnit() {
    return unit;
  }

  @Override
  protected Marshaler getDataMarshaler() {
    return dataMarshaler;
  }

  @Override
  protected ProtoFieldInfo getDataField() {
    return dataField;
  }
}
