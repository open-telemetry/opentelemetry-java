/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim.internal.metrics;

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
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.data.SummaryData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nullable;

/**
 * Adapts an OpenCensus metric into the OpenTelemetry metric data API.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class MetricAdapter {
  private MetricAdapter() {}
  // All OpenCensus metrics come from this shim.
  // VisibleForTesting.
  static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create("io.opentelemetry.opencensusshim");

  // Parser for string value of `io.opencensus.contrib.exemplar.util.AttachmentValueSpanContext`
  // // SpanContext{traceId=TraceId{traceId=(id))}, spanId=SpanId{spanId=(id), ...}
  private static final Pattern OPENCENSUS_TRACE_ATTACHMENT_PATTERN =
      Pattern.compile(
          "SpanContext\\{traceId=TraceId\\{traceId=([0-9A-Ga-g]+)\\}, spanId=SpanId\\{spanId=([0-9A-Ga-g]+)\\},.*\\}");
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
        return ImmutableMetricData.createLongGauge(
            otelResource,
            INSTRUMENTATION_SCOPE_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertLongGauge(censusMetric));
      case GAUGE_DOUBLE:
        return ImmutableMetricData.createDoubleGauge(
            otelResource,
            INSTRUMENTATION_SCOPE_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertDoubleGauge(censusMetric));
      case CUMULATIVE_INT64:
        return ImmutableMetricData.createLongSum(
            otelResource,
            INSTRUMENTATION_SCOPE_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertLongSum(censusMetric));
      case CUMULATIVE_DOUBLE:
        return ImmutableMetricData.createDoubleSum(
            otelResource,
            INSTRUMENTATION_SCOPE_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertDoubleSum(censusMetric));
      case CUMULATIVE_DISTRIBUTION:
        return ImmutableMetricData.createDoubleHistogram(
            otelResource,
            INSTRUMENTATION_SCOPE_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertHistogram(censusMetric));
      case SUMMARY:
        return ImmutableMetricData.createDoubleSummary(
            otelResource,
            INSTRUMENTATION_SCOPE_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertSummary(censusMetric));
      case GAUGE_DISTRIBUTION:
        return ImmutableMetricData.createDoubleHistogram(
            otelResource,
            INSTRUMENTATION_SCOPE_INFO,
            censusMetric.getMetricDescriptor().getName(),
            censusMetric.getMetricDescriptor().getDescription(),
            censusMetric.getMetricDescriptor().getUnit(),
            convertGaugeHistogram(censusMetric));
    }
    // Should be unreachable....
    throw new IllegalArgumentException(
        "Unknown OpenCensus metric type: " + censusMetric.getMetricDescriptor().getType());
  }

  static GaugeData<LongPointData> convertLongGauge(Metric censusMetric) {
    return ImmutableGaugeData.create(convertLongPoints(censusMetric));
  }

  static GaugeData<DoublePointData> convertDoubleGauge(Metric censusMetric) {
    return ImmutableGaugeData.create(convertDoublePoints(censusMetric));
  }

  static SumData<LongPointData> convertLongSum(Metric censusMetric) {
    return ImmutableSumData.create(
        true, AggregationTemporality.CUMULATIVE, convertLongPoints(censusMetric));
  }

  static SumData<DoublePointData> convertDoubleSum(Metric censusMetric) {
    return ImmutableSumData.create(
        true, AggregationTemporality.CUMULATIVE, convertDoublePoints(censusMetric));
  }

  static HistogramData convertHistogram(Metric censusMetric) {
    return ImmutableHistogramData.create(
        AggregationTemporality.CUMULATIVE, convertHistogramPoints(censusMetric));
  }

  static HistogramData convertGaugeHistogram(Metric censusMetric) {
    return ImmutableHistogramData.create(
        AggregationTemporality.DELTA, convertHistogramPoints(censusMetric));
  }

  static SummaryData convertSummary(Metric censusMetric) {
    return ImmutableSummaryData.create(convertSummaryPoints(censusMetric));
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
            ImmutableLongPointData.create(
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
            ImmutableDoublePointData.create(
                startTimestamp,
                mapTimestamp(point.getTimestamp()),
                attributes,
                doubleValue(point)));
      }
    }
    return result;
  }

  static Collection<HistogramPointData> convertHistogramPoints(Metric censusMetric) {
    boolean isGauge =
        censusMetric.getMetricDescriptor().getType() == MetricDescriptor.Type.GAUGE_DISTRIBUTION;
    // TODO - preallocate array to correct size.
    List<HistogramPointData> result = new ArrayList<>();
    for (TimeSeries ts : censusMetric.getTimeSeriesList()) {
      long startTimestamp = mapTimestamp(ts.getStartTimestamp());
      Attributes attributes =
          mapAttributes(censusMetric.getMetricDescriptor().getLabelKeys(), ts.getLabelValues());
      for (Point point : ts.getPoints()) {
        long endTimestamp = mapTimestamp(point.getTimestamp());
        HistogramPointData otelPoint =
            point
                .getValue()
                .match(
                    doubleValue -> null,
                    longValue -> null,
                    distribution ->
                        ImmutableHistogramPointData.create(
                            // Report Gauge histograms as DELTA with "instantaneous" time window.
                            isGauge ? endTimestamp : startTimestamp,
                            endTimestamp,
                            attributes,
                            distribution.getSum(),
                            /* hasMin= */ false,
                            0,
                            /* hasMax= */ false,
                            0,
                            mapBoundaries(distribution.getBucketOptions()),
                            mapCounts(distribution.getBuckets()),
                            mapExemplars(distribution.getBuckets())),
                    summary -> null,
                    defaultValue -> null);
        if (otelPoint != null) {
          result.add(otelPoint);
        }
      }
    }
    return result;
  }

  static Collection<SummaryPointData> convertSummaryPoints(Metric censusMetric) {
    List<SummaryPointData> result = new ArrayList<>();
    for (TimeSeries ts : censusMetric.getTimeSeriesList()) {
      long startTimestamp = mapTimestamp(ts.getStartTimestamp());
      Attributes attributes =
          mapAttributes(censusMetric.getMetricDescriptor().getLabelKeys(), ts.getLabelValues());
      for (Point point : ts.getPoints()) {
        SummaryPointData otelPoint =
            point
                .getValue()
                .match(
                    dv -> null,
                    lv -> null,
                    distribution -> null,
                    summary ->
                        ImmutableSummaryPointData.create(
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

  static List<DoubleExemplarData> mapExemplars(List<Distribution.Bucket> buckets) {
    List<DoubleExemplarData> result = new ArrayList<>();
    for (Distribution.Bucket bucket : buckets) {
      Exemplar exemplar = bucket.getExemplar();
      if (exemplar != null) {
        result.add(mapExemplar(exemplar));
      }
    }
    return result;
  }

  private static DoubleExemplarData mapExemplar(Exemplar exemplar) {
    // Look for trace/span id.
    SpanContext spanContext = SpanContext.getInvalid();
    if (exemplar.getAttachments().containsKey("SpanContext")) {
      // We need to use `io.opencensus.contrib.exemplar.util.AttachmentValueSpanContext`
      // The `toString` will be the following:
      // SpanContext{traceId=TraceId{traceId=(id))}, spanId=SpanId{spanId=(id), ...}
      // We *attempt* parse it rather than pull in yet another dependency.
      String spanContextToString = exemplar.getAttachments().get("SpanContext").getValue();
      Matcher m = OPENCENSUS_TRACE_ATTACHMENT_PATTERN.matcher(spanContextToString);
      if (m.matches()) {
        MatchResult mr = m.toMatchResult();
        String traceId = mr.group(1);
        String spanId = mr.group(2);
        spanContext =
            SpanContext.create(traceId, spanId, TraceFlags.getDefault(), TraceState.getDefault());
      }
    }
    return ImmutableDoubleExemplarData.create(
        Attributes.empty(),
        mapTimestamp(exemplar.getTimestamp()),
        spanContext,
        exemplar.getValue());
  }

  static long mapTimestamp(@Nullable Timestamp time) {
    // Treat all empty timestamps as "0" (proto3)
    if (time == null) {
      return 0;
    }
    return TimeUnit.SECONDS.toNanos(time.getSeconds()) + time.getNanos();
  }

  private static List<ValueAtQuantile> mapValueAtPercentiles(
      List<Summary.Snapshot.ValueAtPercentile> valueAtPercentiles) {
    List<ValueAtQuantile> result = new ArrayList<>(valueAtPercentiles.size());
    for (Summary.Snapshot.ValueAtPercentile censusValue : valueAtPercentiles) {
      result.add(
          ImmutableValueAtQuantile.create(
              censusValue.getPercentile() / 100.0, censusValue.getValue()));
    }
    return result;
  }
}
