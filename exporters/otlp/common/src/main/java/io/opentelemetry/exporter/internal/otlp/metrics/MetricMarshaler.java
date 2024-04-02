/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import static io.opentelemetry.sdk.metrics.data.MetricDataType.DOUBLE_GAUGE;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.DOUBLE_SUM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.EXPONENTIAL_HISTOGRAM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.HISTOGRAM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_GAUGE;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_SUM;
import static io.opentelemetry.sdk.metrics.data.MetricDataType.SUMMARY;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Metric;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
        dataMarshaler = SummaryMarshaler.create(metric.getSummaryData());
        dataField = Metric.SUMMARY;
        break;
      case HISTOGRAM:
        dataMarshaler = HistogramMarshaler.create(metric.getHistogramData());
        dataField = Metric.HISTOGRAM;
        break;
      case EXPONENTIAL_HISTOGRAM:
        dataMarshaler = ExponentialHistogramMarshaler.create(metric.getExponentialHistogramData());
        dataField = Metric.EXPONENTIAL_HISTOGRAM;
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

  public static void writeTo(Serializer output, MetricData metricData, MarshalerContext context)
      throws IOException {
    DataHandler dataHandler = DATA_HANDLERS.get(metricData.getType());
    if (dataHandler == null) {
      // Someone not using BOM to align versions as we require. Just skip the metric.
      return;
    }

    if (context.marshalStringNoAllocation()) {
      output.serializeString(Metric.NAME, metricData.getName(), context.getSize());
      output.serializeString(Metric.DESCRIPTION, metricData.getDescription(), context.getSize());
      output.serializeString(Metric.UNIT, metricData.getUnit(), context.getSize());
    } else {
      byte[] nameUtf8 = context.getByteArray();
      output.serializeString(Metric.NAME, nameUtf8);
      byte[] descriptionUtf8 = context.getByteArray();
      output.serializeString(Metric.DESCRIPTION, descriptionUtf8);
      byte[] unitUtf8 = context.getByteArray();
      output.serializeString(Metric.UNIT, unitUtf8);
    }

    dataHandler.writeTo(output, metricData, context);
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

  public static int calculateSize(MetricData metricData, MarshalerContext context) {
    DataHandler dataHandler = DATA_HANDLERS.get(metricData.getType());
    if (dataHandler == null) {
      // Someone not using BOM to align versions as we require. Just skip the metric.
      return 0;
    }

    int size = 0;
    if (context.marshalStringNoAllocation()) {
      int nameUtf8Size = MarshalerUtil.getUtf8Size(metricData.getName());
      context.addSize(nameUtf8Size);
      size += MarshalerUtil.sizeBytes(Metric.NAME, nameUtf8Size);

      int descriptionUtf8Size = MarshalerUtil.getUtf8Size(metricData.getDescription());
      context.addSize(descriptionUtf8Size);
      size += MarshalerUtil.sizeBytes(Metric.DESCRIPTION, descriptionUtf8Size);

      int unitUtf8Size = MarshalerUtil.getUtf8Size(metricData.getUnit());
      context.addSize(unitUtf8Size);
      size += MarshalerUtil.sizeBytes(Metric.NAME, unitUtf8Size);
    } else {
      byte[] nameUtf8 = MarshalerUtil.toBytes(metricData.getName());
      context.addData(nameUtf8);
      size += MarshalerUtil.sizeBytes(Metric.NAME, nameUtf8);

      byte[] descriptionUtf8 = MarshalerUtil.toBytes(metricData.getDescription());
      context.addData(descriptionUtf8);
      size += MarshalerUtil.sizeBytes(Metric.DESCRIPTION, descriptionUtf8);

      byte[] unitUtf8 = MarshalerUtil.toBytes(metricData.getUnit());
      context.addData(unitUtf8);
      size += MarshalerUtil.sizeBytes(Metric.UNIT, unitUtf8);
    }

    size +=
        MarshalerUtil.sizeMessage(
            dataHandler.dataField, metricData, dataHandler::calculateSize, context);

    return size;
  }

  private static final Map<MetricDataType, DataHandler> DATA_HANDLERS = new HashMap<>();

  static {
    DATA_HANDLERS.put(
        LONG_GAUGE,
        new DataHandler(Metric.GAUGE) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return GaugeMarshaler.calculateSize(metricData.getLongGaugeData(), context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.GAUGE, metric.getLongGaugeData(), GaugeMarshaler::writeTo, context);
          }
        });
    DATA_HANDLERS.put(
        DOUBLE_GAUGE,
        new DataHandler(Metric.GAUGE) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return GaugeMarshaler.calculateSize(metricData.getDoubleGaugeData(), context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.GAUGE, metric.getDoubleGaugeData(), GaugeMarshaler::writeTo, context);
          }
        });
    DATA_HANDLERS.put(
        LONG_SUM,
        new DataHandler(Metric.SUM) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return SumMarshaler.calculateSize(metricData.getLongSumData(), context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.SUM, metric.getLongSumData(), SumMarshaler::writeTo, context);
          }
        });
    DATA_HANDLERS.put(
        DOUBLE_SUM,
        new DataHandler(Metric.SUM) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return SumMarshaler.calculateSize(metricData.getDoubleSumData(), context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.SUM, metric.getDoubleSumData(), SumMarshaler::writeTo, context);
          }
        });
    DATA_HANDLERS.put(
        SUMMARY,
        new DataHandler(Metric.SUMMARY) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return SummaryMarshaler.calculateSize(metricData.getSummaryData(), context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.SUMMARY, metric.getSummaryData(), SummaryMarshaler::writeTo, context);
          }
        });
    DATA_HANDLERS.put(
        HISTOGRAM,
        new DataHandler(Metric.HISTOGRAM) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return HistogramMarshaler.calculateSize(metricData.getHistogramData(), context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.HISTOGRAM, metric.getHistogramData(), HistogramMarshaler::writeTo, context);
          }
        });
    DATA_HANDLERS.put(
        EXPONENTIAL_HISTOGRAM,
        new DataHandler(Metric.EXPONENTIAL_HISTOGRAM) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return ExponentialHistogramMarshaler.calculateSize(
                metricData.getExponentialHistogramData(), context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.EXPONENTIAL_HISTOGRAM,
                metric.getExponentialHistogramData(),
                ExponentialHistogramMarshaler::writeTo,
                context);
          }
        });
  }

  private abstract static class DataHandler {
    final ProtoFieldInfo dataField;

    DataHandler(ProtoFieldInfo dataField) {
      this.dataField = dataField;
    }

    abstract int calculateSize(MetricData metricData, MarshalerContext context);

    abstract void writeTo(Serializer output, MetricData metricData, MarshalerContext context)
        throws IOException;
  }
}
