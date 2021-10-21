/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ProtoFieldInfo;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Metric;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.io.IOException;

final class MetricMarshaler extends MarshalerWithSize {
  private final byte[] nameUtf8;
  private final byte[] descriptionUtf8;
  private final byte[] unitUtf8;

  private final Marshaler dataMarshaler;
  private final ProtoFieldInfo dataField;

  static Marshaler create(MetricData metric) {
    // TODO(anuraaga): Cache these as they should be effectively singleton.
    byte[] name = MarshalerUtil.toBytes(metric.getName());
    byte[] description = MarshalerUtil.toBytes(metric.getDescription());
    byte[] unit = MarshalerUtil.toBytes(metric.getUnit());

    Marshaler dataMarshaler = null;
    ProtoFieldInfo dataField = null;
    switch (metric.getType()) {
      case LONG_GAUGE:
        dataMarshaler = GaugeMarshaler.create(metric.getLongGaugeData());
        dataField = Metric.GAUGE;
        break;
      case DOUBLE_GAUGE:
        dataMarshaler = GaugeMarshaler.create(metric.getDoubleGaugeData());
        dataField = Metric.GAUGE;
        break;
      case LONG_SUM:
        dataMarshaler = SumMarshaler.create(metric.getLongSumData());
        dataField = Metric.SUM;
        break;
      case DOUBLE_SUM:
        dataMarshaler = SumMarshaler.create(metric.getDoubleSumData());
        dataField = Metric.SUM;
        break;
      case SUMMARY:
        dataMarshaler = SummaryMarshaler.create(metric.getDoubleSummaryData());
        dataField = Metric.SUMMARY;
        break;
      case HISTOGRAM:
        dataMarshaler = HistogramMarshaler.create(metric.getDoubleHistogramData());
        dataField = Metric.HISTOGRAM;
        break;
      case EXPONENTIAL_HISTOGRAM:
        throw new UnsupportedOperationException("Exponential Histogram exporter not developed.");
    }

    if (dataMarshaler == null || dataField == null) {
      // Someone not using BOM to align versions as we require. Just skip the metric.
      return NoopMarshaler.INSTANCE;
    }

    return new MetricMarshaler(name, description, unit, dataMarshaler, dataField);
  }

  private MetricMarshaler(
      byte[] nameUtf8,
      byte[] descriptionUtf8,
      byte[] unitUtf8,
      Marshaler dataMarshaler,
      ProtoFieldInfo dataField) {
    super(calculateSize(nameUtf8, descriptionUtf8, unitUtf8, dataMarshaler, dataField));
    this.nameUtf8 = nameUtf8;
    this.descriptionUtf8 = descriptionUtf8;
    this.unitUtf8 = unitUtf8;
    this.dataMarshaler = dataMarshaler;
    this.dataField = dataField;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeString(Metric.NAME, nameUtf8);
    output.serializeString(Metric.DESCRIPTION, descriptionUtf8);
    output.serializeString(Metric.UNIT, unitUtf8);
    output.serializeMessage(dataField, dataMarshaler);
  }

  private static int calculateSize(
      byte[] nameUtf8,
      byte[] descriptionUtf8,
      byte[] unitUtf8,
      Marshaler dataMarshaler,
      ProtoFieldInfo dataField) {
    int size = 0;
    size += MarshalerUtil.sizeBytes(Metric.NAME, nameUtf8);
    size += MarshalerUtil.sizeBytes(Metric.DESCRIPTION, descriptionUtf8);
    size += MarshalerUtil.sizeBytes(Metric.UNIT, unitUtf8);
    size += MarshalerUtil.sizeMessage(dataField, dataMarshaler);
    return size;
  }
}
