/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.metrics;

import io.opencensus.common.Timestamp;
import io.opencensus.metrics.LabelKey;
import io.opencensus.metrics.LabelValue;
import io.opencensus.metrics.data.Exemplar;
import io.opencensus.metrics.export.Distribution;
import io.opencensus.metrics.export.Metric;
import io.opencensus.metrics.export.MetricDescriptor;
import io.opencensus.metrics.export.Point;
import io.opencensus.metrics.export.Summary;
import io.opencensus.metrics.export.TimeSeries;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Adapts an OpenCensus metric into the OpenTelemetry metric data API. */
public final class MetricAdapter {
  private MetricAdapter() {}
  // All OpenCensus metrics come from this shim.
  // VisibleForTesting.
  static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.opencensusshim", null);

  /**
   * Converts an open-census metric into the OTLP format.
   *
   * @param otelResource The resource associated with the opentelemetry SDK.
   * @param censusMetric The OpenCensus metric to convert.
   */
  public static MetricData convert(Resource otelResource, Metric censusMetric) {
    // Note: we can't just adapt interfaces, we need to do full copy because OTel data API uses
    // auto-value vs. pure interfaces.
    switch (censusMetric.getMetricDescriptor().getType()) {
      case GAUGE_INT64:
        return MetricData.createLongGauge(
            otelResource,
            INSTRUMENTATION_LIBRARY_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertLongGauge(censusMetric));
      case GAUGE_DOUBLE:
        return MetricData.createDoubleGauge(
            otelResource,
            INSTRUMENTATION_LIBRARY_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertDoubleGauge(censusMetric));
      case CUMULATIVE_INT64:
        return MetricData.createLongSum(
            otelResource,
            INSTRUMENTATION_LIBRARY_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertLongSum(censusMetric));
      case CUMULATIVE_DOUBLE:
        return MetricData.createDoubleSum(
            otelResource,
            INSTRUMENTATION_LIBRARY_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertDoubleSum(censusMetric));
      case CUMULATIVE_DISTRIBUTION:
        return MetricData.createDoubleHistogram(
            otelResource,
            INSTRUMENTATION_LIBRARY_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertHistogram(censusMetric));
      case SUMMARY:
        return MetricData.createDoubleSummary(
            otelResource,
            INSTRUMENTATION_LIBRARY_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertSummary(censusMetric));
      case GAUGE_DISTRIBUTION:
        return MetricData.createDoubleHistogram(
            otelResource,
            INSTRUMENTATION_LIBRARY_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertGaugeHistogram(censusMetric));
    }
    // Should be unreachable....
    throw new IllegalArgumentException(
        "Unknown OpenCensus metric type: " + censusMetric.getMetricDescriptor().getType());
  }

  static LongGaugeData convertLongGauge(Metric censusMetric) {
    return LongGaugeData.create(convertLongPoints(censusMetric));
  }

  static DoubleGaugeData convertDoubleGauge(Metric censusMetric) {
    return DoubleGaugeData.create(convertDoublePoints(censusMetric));
  }

  static LongSumData convertLongSum(Metric censusMetric) {
    return LongSumData.create(
        true, AggregationTemporality.CUMULATIVE, convertLongPoints(censusMetric));
  }

  static DoubleSumData convertDoubleSum(Metric censusMetric) {
    return DoubleSumData.create(
        true, AggregationTemporality.CUMULATIVE, convertDoublePoints(censusMetric));
  }

  static DoubleHistogramData convertHistogram(Metric censusMetric) {
    return DoubleHistogramData.create(
        AggregationTemporality.CUMULATIVE, convertHistogramPoints(censusMetric));
  }

  static DoubleHistogramData convertGaugeHistogram(Metric censusMetric) {
    return DoubleHistogramData.create(
        AggregationTemporality.DELTA, convertHistogramPoints(censusMetric));
  }

  static DoubleSummaryData convertSummary(Metric censusMetric) {
    return DoubleSummaryData.create(convertSummaryPoints(censusMetric));
  }

  static Collection<LongPointData> convertLongPoints(Metric censusMetric) {
    // TODO - preallocate array to correct size.
    List<LongPointData> result = new ArrayList<>();
    for (TimeSeries ts : censusMetric.getTimeSeriesList()) {
      long startTimestamp = mapTimestamp(ts.getStartTimestamp());
      Attributes attributes =
          mapAttributes(censusMetric.getMetricDescriptor().getLabelKeys(), ts.getLabelValues());
      for (Point point : ts.getPoints()) {
        result.add(
            LongPointData.create(
                startTimestamp, mapTimestamp(point.getTimestamp()), attributes, longValue(point)));
      }
    }
    return result;
  }

  static Collection<DoublePointData> convertDoublePoints(Metric censusMetric) {
    // TODO - preallocate array to correct size.
    List<DoublePointData> result = new ArrayList<>();
    for (TimeSeries ts : censusMetric.getTimeSeriesList()) {
      long startTimestamp = mapTimestamp(ts.getStartTimestamp());
      Attributes attributes =
          mapAttributes(censusMetric.getMetricDescriptor().getLabelKeys(), ts.getLabelValues());
      for (Point point : ts.getPoints()) {
        result.add(
            DoublePointData.create(
                startTimestamp,
                mapTimestamp(point.getTimestamp()),
                attributes,
                doubleValue(point)));
      }
    }
    return result;
  }

  static Collection<DoubleHistogramPointData> convertHistogramPoints(Metric censusMetric) {
    boolean isGauge =
        censusMetric.getMetricDescriptor().getType() == MetricDescriptor.Type.GAUGE_DISTRIBUTION;
    // TODO - preallocate array to correct size.
    List<DoubleHistogramPointData> result = new ArrayList<>();
    for (TimeSeries ts : censusMetric.getTimeSeriesList()) {
      long startTimestamp = mapTimestamp(ts.getStartTimestamp());
      Attributes attributes =
          mapAttributes(censusMetric.getMetricDescriptor().getLabelKeys(), ts.getLabelValues());
      for (Point point : ts.getPoints()) {
        long endTimestamp = mapTimestamp(point.getTimestamp());
        DoubleHistogramPointData otelPoint =
            point
                .getValue()
                .match(
                    doubleValue -> null,
                    longValue -> null,
                    distribution ->
                        DoubleHistogramPointData.create(
                            // Report Gauge histograms as DELTA with "instantaneous" time window.
                            isGauge ? endTimestamp : startTimestamp,
                            endTimestamp,
                            attributes,
                            distribution.getSum(),
                            mapBoundaries(distribution.getBucketOptions()),
                            mapCounts(distribution.getBuckets()),
                            mapExemplars(distribution.getBuckets())),
                    sumamry -> null,
                    defaultValue -> null);
        if (otelPoint != null) {
          result.add(otelPoint);
        }
      }
    }
    return result;
  }

  static Collection<DoubleSummaryPointData> convertSummaryPoints(Metric censusMetric) {
    List<DoubleSummaryPointData> result = new ArrayList<>();
    for (TimeSeries ts : censusMetric.getTimeSeriesList()) {
      long startTimestamp = mapTimestamp(ts.getStartTimestamp());
      Attributes attributes =
          mapAttributes(censusMetric.getMetricDescriptor().getLabelKeys(), ts.getLabelValues());
      for (Point point : ts.getPoints()) {
        DoubleSummaryPointData otelPoint =
            point
                .getValue()
                .match(
                    dv -> null,
                    lv -> null,
                    distribution -> null,
                    summary ->
                        DoubleSummaryPointData.create(
                            startTimestamp,
                            mapTimestamp(point.getTimestamp()),
                            attributes,
                            summary.getCount(),
                            summary.getSum(),
                            mapValueAtPercentiles(summary.getSnapshot().getValueAtPercentiles())),
                    defaultValue -> null);
        if (otelPoint != null) {
          result.add(otelPoint);
        }
      }
    }
    return result;
  }

  static Attributes mapAttributes(List<LabelKey> labels, List<LabelValue> values) {
    AttributesBuilder result = Attributes.builder();
    for (int i = 0; i < labels.size(); i++) {
      result.put(labels.get(i).getKey(), values.get(i).getValue());
    }
    return result.build();
  }

  static long longValue(Point point) {
    return point
        .getValue()
        .match(
            Double::longValue,
            lv -> lv,
            // Ignore these cases (logic error)
            distribution -> 0,
            summary -> 0,
            defaultValue -> 0)
        .longValue();
  }

  static double doubleValue(Point point) {
    return point
        .getValue()
        .match(
            d -> d,
            Long::doubleValue,
            // Ignore these cases (logic error)
            distribution -> 0,
            summary -> 0,
            defaultValue -> 0)
        .doubleValue();
  }

  static List<Double> mapBoundaries(Distribution.BucketOptions censusBuckets) {
    return censusBuckets.match(
        explicit -> explicit.getBucketBoundaries(), defaultOption -> Collections.emptyList());
  }

  static List<Long> mapCounts(List<Distribution.Bucket> buckets) {
    List<Long> result = new ArrayList<>(buckets.size());
    for (Distribution.Bucket bucket : buckets) {
      result.add(bucket.getCount());
    }
    return result;
  }

  static List<ExemplarData> mapExemplars(List<Distribution.Bucket> buckets) {
    List<ExemplarData> result = new ArrayList<>();
    for (Distribution.Bucket bucket : buckets) {
      Exemplar exemplar = bucket.getExemplar();
      if (exemplar != null) {
        result.add(mapExemplar(exemplar));
      }
    }
    return result;
  }

  private static ExemplarData mapExemplar(Exemplar exemplar) {
    // Look for trace/span id.
    String spanId = null;
    String traceId = null;
    if (exemplar.getAttachments().containsKey("SpanContext")) {
      // We need to use `io.opencensus.contrib.exemplar.util.AttachmentValueSpanContext`
      // The `toString` will be the following:
      // SpanContext(traceId={traceId}, spanId={spanId}, traceOptions={traceOptions})
      // We *attempt* parse it rather than pull in yet another dependency.
      String spanContextToString = exemplar.getAttachments().get("SpanContext").getValue();
      Matcher m =
          Pattern.compile("SpanContext\\(traceId=([0-9A-Ga-g]+), spanId=([0-9A-Ga-g]+),.*\\)")
              .matcher(spanContextToString);
      if (m.matches()) {
        MatchResult mr = m.toMatchResult();
        traceId = mr.group(1);
        spanId = mr.group(2);
      }
    }
    return DoubleExemplarData.create(
        Attributes.empty(),
        mapTimestamp(exemplar.getTimestamp()),
        spanId,
        traceId,
        exemplar.getValue());
  }

  static long mapTimestamp(Timestamp time) {
    return TimeUnit.SECONDS.toNanos(time.getSeconds()) + time.getNanos();
  }

  private static List<ValueAtPercentile> mapValueAtPercentiles(
      List<Summary.Snapshot.ValueAtPercentile> valueAtPercentiles) {
    List<ValueAtPercentile> result = new ArrayList<>(valueAtPercentiles.size());
    for (Summary.Snapshot.ValueAtPercentile censusValue : valueAtPercentiles) {
      result.add(ValueAtPercentile.create(censusValue.getPercentile(), censusValue.getValue()));
    }
    return result;
  }
}
