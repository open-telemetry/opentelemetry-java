package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.proto.metrics.v1.internal.Metric;
import io.opentelemetry.sdk.metrics.data.MetricData;

final class ImmutableMetricMarshaler extends MetricMarshaler {
  private final String name;
  private final String description;
  private final String unit;

  private final Marshaler dataMarshaler;
  private final ProtoFieldInfo dataField;
  private final int size;

  static Marshaler create(MetricData metric) {
    Marshaler dataMarshaler = null;
    ProtoFieldInfo dataField = null;
    switch (metric.getType()) {
      case LONG_GAUGE:
        // TODO Asaf: Change to immutable
        dataMarshaler = GaugeMarshaler.create(metric.getLongGaugeData());
        dataField = Metric.GAUGE;
        break;
      case DOUBLE_GAUGE:
        // TODO Asaf: Change to immutable
        dataMarshaler = GaugeMarshaler.create(metric.getDoubleGaugeData());
        dataField = Metric.GAUGE;
        break;
      case LONG_SUM:
        dataMarshaler = ImmutableSumMarshaler.create(metric.getLongSumData());
        dataField = Metric.SUM;
        break;
      case DOUBLE_SUM:
        dataMarshaler = ImmutableSumMarshaler.create(metric.getDoubleSumData());
        dataField = Metric.SUM;
        break;
      case SUMMARY:
        // TODO Asaf: Change to immutable
        dataMarshaler = SummaryMarshaler.create(metric.getSummaryData());
        dataField = Metric.SUMMARY;
        break;
      case HISTOGRAM:
        // TODO Asaf: Change to immutable
        dataMarshaler = HistogramMarshaler.create(metric.getHistogramData());
        dataField = Metric.HISTOGRAM;
        break;
      case EXPONENTIAL_HISTOGRAM:
        // TODO Asaf: Change to immutable
        dataMarshaler = ExponentialHistogramMarshaler.create(metric.getExponentialHistogramData());
        dataField = Metric.EXPONENTIAL_HISTOGRAM;
    }

    if (dataMarshaler == null || dataField == null) {
      // Someone not using BOM to align versions as we require. Just skip the metric.
      return NoopMarshaler.INSTANCE;
    }

    return new ImmutableMetricMarshaler(
        metric.getName(),
        metric.getDescription(),
        metric.getUnit(),
        dataMarshaler,
        dataField);
  }

  protected ImmutableMetricMarshaler(
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
