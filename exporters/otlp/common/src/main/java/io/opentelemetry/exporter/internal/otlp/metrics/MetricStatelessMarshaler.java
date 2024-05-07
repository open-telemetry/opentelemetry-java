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
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.metrics.v1.internal.Metric;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

/** See {@link MetricMarshaler}. */
final class MetricStatelessMarshaler implements StatelessMarshaler<MetricData> {
  static final MetricStatelessMarshaler INSTANCE = new MetricStatelessMarshaler();
  private static final Map<MetricDataType, StatelessMarshaler<MetricData>> METRIC_MARSHALERS =
      new EnumMap<>(MetricDataType.class);

  static {
    METRIC_MARSHALERS.put(
        LONG_GAUGE,
        new StatelessMarshaler<MetricData>() {
          @Override
          public int getBinarySerializedSize(MetricData metricData, MarshalerContext context) {
            return StatelessMarshalerUtil.sizeMessageWithContext(
                Metric.GAUGE,
                metricData.getLongGaugeData(),
                GaugeStatelessMarshaler.INSTANCE,
                context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessageWithContext(
                Metric.GAUGE, metric.getLongGaugeData(), GaugeStatelessMarshaler.INSTANCE, context);
          }
        });
    METRIC_MARSHALERS.put(
        DOUBLE_GAUGE,
        new StatelessMarshaler<MetricData>() {
          @Override
          public int getBinarySerializedSize(MetricData metricData, MarshalerContext context) {
            return StatelessMarshalerUtil.sizeMessageWithContext(
                Metric.GAUGE,
                metricData.getDoubleGaugeData(),
                GaugeStatelessMarshaler.INSTANCE,
                context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessageWithContext(
                Metric.GAUGE,
                metric.getDoubleGaugeData(),
                GaugeStatelessMarshaler.INSTANCE,
                context);
          }
        });
    METRIC_MARSHALERS.put(
        LONG_SUM,
        new StatelessMarshaler<MetricData>() {
          @Override
          public int getBinarySerializedSize(MetricData metricData, MarshalerContext context) {
            return StatelessMarshalerUtil.sizeMessageWithContext(
                Metric.SUM, metricData.getLongSumData(), SumStatelessMarshaler.INSTANCE, context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessageWithContext(
                Metric.SUM, metric.getLongSumData(), SumStatelessMarshaler.INSTANCE, context);
          }
        });
    METRIC_MARSHALERS.put(
        DOUBLE_SUM,
        new StatelessMarshaler<MetricData>() {
          @Override
          public int getBinarySerializedSize(MetricData metricData, MarshalerContext context) {
            return StatelessMarshalerUtil.sizeMessageWithContext(
                Metric.SUM, metricData.getDoubleSumData(), SumStatelessMarshaler.INSTANCE, context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessageWithContext(
                Metric.SUM, metric.getDoubleSumData(), SumStatelessMarshaler.INSTANCE, context);
          }
        });
    METRIC_MARSHALERS.put(
        SUMMARY,
        new StatelessMarshaler<MetricData>() {
          @Override
          public int getBinarySerializedSize(MetricData metricData, MarshalerContext context) {
            return StatelessMarshalerUtil.sizeMessageWithContext(
                Metric.SUMMARY,
                metricData.getSummaryData(),
                SummaryStatelessMarshaler.INSTANCE,
                context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessageWithContext(
                Metric.SUMMARY,
                metric.getSummaryData(),
                SummaryStatelessMarshaler.INSTANCE,
                context);
          }
        });
    METRIC_MARSHALERS.put(
        HISTOGRAM,
        new StatelessMarshaler<MetricData>() {
          @Override
          public int getBinarySerializedSize(MetricData metricData, MarshalerContext context) {
            return StatelessMarshalerUtil.sizeMessageWithContext(
                Metric.HISTOGRAM,
                metricData.getHistogramData(),
                HistogramStatelessMarshaler.INSTANCE,
                context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessageWithContext(
                Metric.HISTOGRAM,
                metric.getHistogramData(),
                HistogramStatelessMarshaler.INSTANCE,
                context);
          }
        });
    METRIC_MARSHALERS.put(
        EXPONENTIAL_HISTOGRAM,
        new StatelessMarshaler<MetricData>() {
          @Override
          public int getBinarySerializedSize(MetricData metricData, MarshalerContext context) {
            return StatelessMarshalerUtil.sizeMessageWithContext(
                Metric.EXPONENTIAL_HISTOGRAM,
                metricData.getExponentialHistogramData(),
                ExponentialHistogramStatelessMarshaler.INSTANCE,
                context);
          }

          @Override
          public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
              throws IOException {
            output.serializeMessageWithContext(
                Metric.EXPONENTIAL_HISTOGRAM,
                metric.getExponentialHistogramData(),
                ExponentialHistogramStatelessMarshaler.INSTANCE,
                context);
          }
        });
  }

  private MetricStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, MetricData metric, MarshalerContext context)
      throws IOException {
    StatelessMarshaler<MetricData> metricMarshaler = METRIC_MARSHALERS.get(metric.getType());
    if (metricMarshaler == null) {
      // Someone not using BOM to align versions as we require. Just skip the metric.
      return;
    }

    output.serializeStringWithContext(Metric.NAME, metric.getName(), context);
    output.serializeStringWithContext(Metric.DESCRIPTION, metric.getDescription(), context);
    output.serializeStringWithContext(Metric.UNIT, metric.getUnit(), context);

    metricMarshaler.writeTo(output, metric, context);
  }

  @Override
  public int getBinarySerializedSize(MetricData metric, MarshalerContext context) {
    StatelessMarshaler<MetricData> metricMarshaler = METRIC_MARSHALERS.get(metric.getType());
    if (metricMarshaler == null) {
      // Someone not using BOM to align versions as we require. Just skip the metric.
      return 0;
    }

    int size = 0;
    size += StatelessMarshalerUtil.sizeStringWithContext(Metric.NAME, metric.getName(), context);
    size +=
        StatelessMarshalerUtil.sizeStringWithContext(
            Metric.DESCRIPTION, metric.getDescription(), context);
    size += StatelessMarshalerUtil.sizeStringWithContext(Metric.UNIT, metric.getUnit(), context);

    size += metricMarshaler.getBinarySerializedSize(metric, context);

    return size;
  }
}
