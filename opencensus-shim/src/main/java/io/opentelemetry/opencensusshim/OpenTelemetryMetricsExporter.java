/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import com.google.common.base.Joiner;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

public class OpenTelemetryMetricsExporter extends MetricExporter {
  private static final Logger LOGGER =
      Logger.getLogger(OpenTelemetryMetricsExporter.class.getName());

  private static final String EXPORTER_NAME = "OpenTelemetryMetricExporter";
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.opencensusshim", null);

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
    List<MetricData> metricData = new ArrayList<>();
    Set<MetricDescriptor.Type> unsupportedTypes = new HashSet<>();
    for (Metric metric : metrics) {
      for (TimeSeries timeSeries : metric.getTimeSeriesList()) {
        LabelsBuilder labelsBuilder = Labels.builder();
        for (int i = 0; i < metric.getMetricDescriptor().getLabelKeys().size(); i++) {
          if (timeSeries.getLabelValues().get(i).getValue() != null) {
            labelsBuilder.put(
                metric.getMetricDescriptor().getLabelKeys().get(i).getKey(),
                timeSeries.getLabelValues().get(i).getValue());
          }
        }
        Labels labels = labelsBuilder.build();
        List<MetricData.Point> points = new ArrayList<>();
        MetricDescriptor.Type type = null;
        for (Point point : timeSeries.getPoints()) {
          type = mapAndAddPoint(unsupportedTypes, metric, labels, points, point);
        }
        MetricData.Type metricDataType = mapType(type);
        if (metricDataType != null) {
          metricData.add(
              MetricData.create(
                  Resource.getDefault(),
                  INSTRUMENTATION_LIBRARY_INFO,
                  metric.getMetricDescriptor().getName(),
                  metric.getMetricDescriptor().getDescription(),
                  metric.getMetricDescriptor().getUnit(),
                  metricDataType,
                  points));
        }
      }
    }
    if (!unsupportedTypes.isEmpty()) {
      LOGGER.warning(
          Joiner.on(",").join(unsupportedTypes)
              + " not supported by OpenCensus to OpenTelemetry migrator.");
    }
    if (!metricData.isEmpty()) {
      otelExporter.export(metricData);
    }
  }

  @Nonnull
  private static MetricDescriptor.Type mapAndAddPoint(
      Set<MetricDescriptor.Type> unsupportedTypes,
      Metric metric,
      Labels labels,
      List<MetricData.Point> points,
      Point point) {
    long timestampNanos =
        TimeUnit.SECONDS.toNanos(point.getTimestamp().getSeconds())
            + point.getTimestamp().getNanos();
    MetricDescriptor.Type type = metric.getMetricDescriptor().getType();
    switch (type) {
      case GAUGE_INT64:
      case CUMULATIVE_INT64:
        points.add(mapLongPoint(labels, point, timestampNanos));
        break;
      case GAUGE_DOUBLE:
      case CUMULATIVE_DOUBLE:
        points.add(mapDoublePoint(labels, point, timestampNanos));
        break;
      case SUMMARY:
        points.add(mapSummaryPoint(labels, point, timestampNanos));
        break;
      default:
        unsupportedTypes.add(type);
        break;
    }
    return type;
  }

  public void stop() {
    intervalMetricReader.stop();
  }

  @Nonnull
  private static SummaryPoint mapSummaryPoint(Labels labels, Point point, long timestampNanos) {
    return SummaryPoint.create(
        timestampNanos,
        timestampNanos,
        labels,
        point
            .getValue()
            .match(arg -> null, arg -> null, arg -> null, Summary::getCount, arg -> null),
        point.getValue().match(arg -> null, arg -> null, arg -> null, Summary::getSum, arg -> null),
        point
            .getValue()
            .match(
                arg -> null,
                arg -> null,
                arg -> null,
                OpenTelemetryMetricsExporter::mapPercentiles,
                arg -> null));
  }

  private static List<ValueAtPercentile> mapPercentiles(Summary arg) {
    List<ValueAtPercentile> percentiles = new ArrayList<>();
    for (Snapshot.ValueAtPercentile percentile : arg.getSnapshot().getValueAtPercentiles()) {
      percentiles.add(ValueAtPercentile.create(percentile.getPercentile(), percentile.getValue()));
    }
    return percentiles;
  }

  @Nonnull
  private static DoublePoint mapDoublePoint(Labels labels, Point point, long timestampNanos) {
    return DoublePoint.create(
        timestampNanos,
        timestampNanos,
        labels,
        point
            .getValue()
            .match(arg -> arg, Long::doubleValue, arg -> null, arg -> null, arg -> null));
  }

  @Nonnull
  private static LongPoint mapLongPoint(Labels labels, Point point, long timestampNanos) {
    return LongPoint.create(
        timestampNanos,
        timestampNanos,
        labels,
        point
            .getValue()
            .match(Double::longValue, arg -> arg, arg -> null, arg -> null, arg -> null));
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
        return MetricData.Type.SUM_LONG;
      case CUMULATIVE_DOUBLE:
        return MetricData.Type.SUM_DOUBLE;
      case SUMMARY:
        return MetricData.Type.SUMMARY;
      default:
        return null;
    }
  }
}
