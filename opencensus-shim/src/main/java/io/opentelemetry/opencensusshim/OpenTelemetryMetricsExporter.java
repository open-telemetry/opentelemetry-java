/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import io.opencensus.common.Duration;
import io.opencensus.exporter.metrics.util.IntervalMetricReader;
import io.opencensus.exporter.metrics.util.MetricExporter;
import io.opencensus.exporter.metrics.util.MetricReader;
import io.opencensus.metrics.Metrics;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.Summary.Snapshot;
import io.opencensus.metrics.export.TimeSeries;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.common.LabelsBuilder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class OpenTelemetryMetricsExporter extends MetricExporter {
  private static final String EXPORTER_NAME = "OpenTelemetryMetricExporter";
  private static final Logger LOGGER =
      Logger.getLogger(OpenTelemetryMetricsExporter.class.getName());

  private final IntervalMetricReader intervalMetricReader;
  private final io.opentelemetry.sdk.metrics.export.MetricExporter otelExporter;

  public static OpenTelemetryMetricsExporter createAndRegister(
      io.opentelemetry.sdk.metrics.export.MetricExporter otelExporter) {
    return new OpenTelemetryMetricsExporter(otelExporter, Duration.create(60, 0));
  }

  public static OpenTelemetryMetricsExporter createAndRegister(
      io.opentelemetry.sdk.metrics.export.MetricExporter otelExporter, Duration exportInterval) {
    return new OpenTelemetryMetricsExporter(otelExporter, exportInterval);
  }

  private OpenTelemetryMetricsExporter(
      io.opentelemetry.sdk.metrics.export.MetricExporter otelExporter, Duration exportInterval) {
    this.otelExporter = otelExporter;
    IntervalMetricReader.Options.Builder options = IntervalMetricReader.Options.builder();
    MetricReader reader =
        MetricReader.create(
            MetricReader.Options.builder()
                .setMetricProducerManager(Metrics.getExportComponent().getMetricProducerManager())
                .setSpanName(EXPORTER_NAME)
                .build());
    intervalMetricReader =
        IntervalMetricReader.create(
            this, reader, options.setExportInterval(exportInterval).build());
  }

  @Override
  public void export(Collection<Metric> metrics) {
    ArrayList<MetricData> metricData = new ArrayList<>();
    for (Metric metric : metrics) {
      for (TimeSeries timeSeries : metric.getTimeSeriesList()) {
        LabelsBuilder labelsBuilder = Labels.builder();
        IntStream.range(0, metric.getMetricDescriptor().getLabelKeys().size())
            .forEach(
                i ->
                    labelsBuilder.put(
                        metric.getMetricDescriptor().getLabelKeys().get(i).getKey(),
                        Objects.requireNonNull(timeSeries.getLabelValues().get(i).getValue())));
        Labels labels = labelsBuilder.build();
        ArrayList<MetricData.Point> points = new ArrayList<>();
        MetricDescriptor.Type type = null;
        for (Point point : timeSeries.getPoints()) {
          long epochNanos =
              TimeUnit.SECONDS.toNanos(point.getTimestamp().getSeconds())
                  + point.getTimestamp().getNanos();
          type = metric.getMetricDescriptor().getType();
          switch (type) {
            case GAUGE_INT64:
            case CUMULATIVE_INT64:
              points.add(
                  LongPoint.create(
                      epochNanos,
                      epochNanos,
                      labels,
                      point
                          .getValue()
                          .match(
                              Double::longValue,
                              arg -> arg,
                              arg -> null,
                              arg -> null,
                              arg -> null)));
              break;
            case GAUGE_DOUBLE:
            case CUMULATIVE_DOUBLE:
              points.add(
                  DoublePoint.create(
                      epochNanos,
                      epochNanos,
                      labels,
                      point
                          .getValue()
                          .match(
                              arg -> arg,
                              Long::doubleValue,
                              arg -> null,
                              arg -> null,
                              arg -> null)));
              break;
            case SUMMARY:
              points.add(
                  SummaryPoint.create(
                      epochNanos,
                      epochNanos,
                      labels,
                      point
                          .getValue()
                          .match(
                              arg -> null,
                              arg -> null,
                              arg -> null,
                              Summary::getCount,
                              arg -> null),
                      point
                          .getValue()
                          .match(
                              arg -> null, arg -> null, arg -> null, Summary::getSum, arg -> null),
                      point
                          .getValue()
                          .match(
                              arg -> null,
                              arg -> null,
                              arg -> null,
                              OpenTelemetryMetricsExporter::mapPercentiles,
                              arg -> null)));
              break;
            default:
              LOGGER.warning(type + " not supported by OpenCensus to OpenTelemetry migrator.");
              break;
          }
        }
        MetricData.Type metricDataType = mapType(type);
        if (metricDataType != null) {
          metricData.add(
              MetricData.create(
                  Resource.getDefault(),
                  InstrumentationLibraryInfo.getEmpty(),
                  metric.getMetricDescriptor().getName(),
                  metric.getMetricDescriptor().getDescription(),
                  metric.getMetricDescriptor().getUnit(),
                  metricDataType,
                  points));
        }
      }
    }
    if (!metricData.isEmpty()) {
      otelExporter.export(metricData);
    }
  }

  private static MetricData.Type mapType(MetricDescriptor.Type type) {
    if (type == null) {
      return null;
    }
    switch (type) {
      case GAUGE_INT64:
        return MetricData.Type.GAUGE_LONG;
      case GAUGE_DOUBLE:
        return MetricData.Type.GAUGE_DOUBLE;
      case CUMULATIVE_INT64:
        return MetricData.Type.MONOTONIC_LONG;
      case CUMULATIVE_DOUBLE:
        return MetricData.Type.MONOTONIC_DOUBLE;
      case SUMMARY:
        return MetricData.Type.SUMMARY;
      default:
        LOGGER.warning(type + " not supported by OpenCensus to OpenTelemetry migrator.");
        return null;
    }
  }

  private static List<ValueAtPercentile> mapPercentiles(Summary arg) {
    ArrayList<ValueAtPercentile> percentiles = new ArrayList<>();
    for (Snapshot.ValueAtPercentile percentile : arg.getSnapshot().getValueAtPercentiles()) {
      percentiles.add(ValueAtPercentile.create(percentile.getPercentile(), percentile.getValue()));
    }
    return percentiles;
  }

  public void stop() {
    intervalMetricReader.stop();
  }
}
