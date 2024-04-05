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

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.Metric;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

final class MetricStatelessMarshaler implements StatelessMarshaler<MetricData> {
  static final MetricStatelessMarshaler INSTANCE = new MetricStatelessMarshaler();
  private static final Map<MetricDataType, DataHandler> DATA_HANDLERS = new HashMap<>();

  static {
    DATA_HANDLERS.put(
        LONG_GAUGE,
        new DataHandler(Metric.GAUGE) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return MarshalerUtil.sizeMessage(
                dataField,
                metricData.getLongGaugeData(),
                GaugeStatelessMarshaler.INSTANCE,
                context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.GAUGE, metric.getLongGaugeData(), GaugeStatelessMarshaler.INSTANCE, context);
          }
        });
    DATA_HANDLERS.put(
        DOUBLE_GAUGE,
        new DataHandler(Metric.GAUGE) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return MarshalerUtil.sizeMessage(
                dataField,
                metricData.getDoubleGaugeData(),
                GaugeStatelessMarshaler.INSTANCE,
                context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.GAUGE,
                metric.getDoubleGaugeData(),
                GaugeStatelessMarshaler.INSTANCE,
                context);
          }
        });
    DATA_HANDLERS.put(
        LONG_SUM,
        new DataHandler(Metric.SUM) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return MarshalerUtil.sizeMessage(
                dataField, metricData.getLongSumData(), SumStatelessMarshaler.INSTANCE, context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.SUM, metric.getLongSumData(), SumStatelessMarshaler.INSTANCE, context);
          }
        });
    DATA_HANDLERS.put(
        DOUBLE_SUM,
        new DataHandler(Metric.SUM) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return MarshalerUtil.sizeMessage(
                dataField, metricData.getDoubleSumData(), SumStatelessMarshaler.INSTANCE, context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.SUM, metric.getDoubleSumData(), SumStatelessMarshaler.INSTANCE, context);
          }
        });
    DATA_HANDLERS.put(
        SUMMARY,
        new DataHandler(Metric.SUMMARY) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return MarshalerUtil.sizeMessage(
                dataField,
                metricData.getSummaryData(),
                SummaryStatelessMarshaler.INSTANCE,
                context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.SUMMARY,
                metric.getSummaryData(),
                SummaryStatelessMarshaler.INSTANCE,
                context);
          }
        });
    DATA_HANDLERS.put(
        HISTOGRAM,
        new DataHandler(Metric.HISTOGRAM) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return MarshalerUtil.sizeMessage(
                dataField,
                metricData.getHistogramData(),
                HistogramStatelessMarshaler.INSTANCE,
                context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.HISTOGRAM,
                metric.getHistogramData(),
                HistogramStatelessMarshaler.INSTANCE,
                context);
          }
        });
    DATA_HANDLERS.put(
        EXPONENTIAL_HISTOGRAM,
        new DataHandler(Metric.EXPONENTIAL_HISTOGRAM) {
          @Override
          public int calculateSize(MetricData metricData, MarshalerContext context) {
            return MarshalerUtil.sizeMessage(
                dataField,
                metricData.getExponentialHistogramData(),
                ExponentialHistogramStatelessMarshaler.INSTANCE,
                context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessage(
                Metric.EXPONENTIAL_HISTOGRAM,
                metric.getExponentialHistogramData(),
                ExponentialHistogramStatelessMarshaler.INSTANCE,
                context);
          }
        });
  }

  @Override
  public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
      throws IOException {
    DataHandler dataHandler = DATA_HANDLERS.get(metric.getType());
    if (dataHandler == null) {
      // Someone not using BOM to align versions as we require. Just skip the metric.
      return;
    }

    output.serializeString(Metric.NAME, metric.getName(), context);
    output.serializeString(Metric.DESCRIPTION, metric.getDescription(), context);
    output.serializeString(Metric.UNIT, metric.getUnit(), context);

    dataHandler.writeTo(output, metric, context);
  }

  @Override
  public int getBinarySerializedSize(MetricData metric, MarshalerContext context) {
    DataHandler dataHandler = DATA_HANDLERS.get(metric.getType());
    if (dataHandler == null) {
      // Someone not using BOM to align versions as we require. Just skip the metric.
      return 0;
    }

    int size = 0;
    size += MarshalerUtil.sizeString(Metric.NAME, metric.getName(), context);
    size += MarshalerUtil.sizeString(Metric.DESCRIPTION, metric.getDescription(), context);
    size += MarshalerUtil.sizeString(Metric.UNIT, metric.getUnit(), context);

    size += dataHandler.calculateSize(metric, context);

    return size;
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
