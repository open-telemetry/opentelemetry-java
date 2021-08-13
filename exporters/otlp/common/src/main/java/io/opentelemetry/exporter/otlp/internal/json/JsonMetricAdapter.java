/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.json;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.exporter.otlp.internal.MetricAdapter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import io.opentelemetry.sdk.metrics.data.LongExemplar;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Converter from SDK {@link MetricData} to OTLP JSON. */
public final class JsonMetricAdapter {
    private static final ThrottlingLogger logger =
            new ThrottlingLogger(Logger.getLogger(MetricAdapter.class.getName()));

    /** Converts the provided {@link MetricData} to JSON. */
    public static JSONArray toJsonResourceMetrics(Collection<MetricData> metricData) {
        Map<Resource, Map<InstrumentationLibraryInfo, JSONArray>> resourceAndLibraryMap =
                groupByResourceAndLibrary(metricData);
        JSONArray resourceMetrics = new JSONArray(resourceAndLibraryMap.size());
        for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, JSONArray>> entryResource :
                resourceAndLibraryMap.entrySet()) {
            JSONArray instrumentationLibraryMetrics =
                    new JSONArray(entryResource.getValue().size());
            for (Map.Entry<InstrumentationLibraryInfo, JSONArray> entryLibrary :
                    entryResource.getValue().entrySet()) {
                instrumentationLibraryMetrics.add(buildInstrumentationLibraryMetrics(entryLibrary));
            }
            resourceMetrics.add(
                    buildResourceMetrics(entryResource.getKey(), instrumentationLibraryMetrics));
        }
        return resourceMetrics;
    }

    private static JSONObject buildResourceMetrics(
            Resource resource, JSONArray instrumentationLibraryMetrics) {
        JSONObject resourceMetricsBuilder = new JSONObject();
        resourceMetricsBuilder.put("resource", JsonResourceAdapter.toProtoResource(resource));
        resourceMetricsBuilder.put("instrumentation_library_metrics", instrumentationLibraryMetrics);

        String schemaUrl = resource.getSchemaUrl();
        if (schemaUrl != null) {
            resourceMetricsBuilder.put("schema_url", schemaUrl);
        }
        return resourceMetricsBuilder;
    }

    private static JSONObject buildInstrumentationLibraryMetrics(
            Map.Entry<InstrumentationLibraryInfo, JSONArray> entryLibrary) {
        JSONObject metricsBuilder = new JSONObject();
        metricsBuilder.put("instrumentation_library", JsonCommonAdapter.toProtoInstrumentationLibrary(entryLibrary.getKey()));
        metricsBuilder.put("metrics", entryLibrary.getValue());
        if (entryLibrary.getKey().getSchemaUrl() != null) {
            metricsBuilder.put("schema_url", entryLibrary.getKey().getSchemaUrl());
        }
        return metricsBuilder;
    }

    private static Map<Resource, Map<InstrumentationLibraryInfo, JSONArray>>
    groupByResourceAndLibrary(Collection<MetricData> metricDataList) {
        Map<Resource, Map<InstrumentationLibraryInfo, JSONArray>> result = new HashMap<>();
        for (MetricData metricData : metricDataList) {
            if (metricData.isEmpty()) {
                // If no points available then ignore.
                continue;
            }

            Resource resource = metricData.getResource();
            Map<InstrumentationLibraryInfo, JSONArray> libraryInfoListMap =
                    result.get(metricData.getResource());
            if (libraryInfoListMap == null) {
                libraryInfoListMap = new HashMap<>();
                result.put(resource, libraryInfoListMap);
            }
            JSONArray metricList =
                    libraryInfoListMap.computeIfAbsent(
                            metricData.getInstrumentationLibraryInfo(), k -> new JSONArray());
            metricList.add(toProtoMetric(metricData));
        }
        return result;
    }


    // fall through comment isn't working for some reason.
    @SuppressWarnings("fallthrough")
    static JSONObject toProtoMetric(MetricData metricData) {
        JSONObject builder = new JSONObject();
        builder.put("name", metricData.getName());
        builder.put("description", metricData.getDescription());
        builder.put("unit", metricData.getUnit());

        switch (metricData.getType()) {
            case LONG_SUM: {
                LongSumData longSumData = metricData.getLongSumData();
                JSONObject sum = new JSONObject();
                sum.put("is_monotonic", longSumData.isMonotonic());
                sum.put("aggregation_temporality", mapToTemporality(longSumData.getAggregationTemporality()));
                JSONArray dataPoints = new JSONArray();
                dataPoints.addAll(toIntDataPoints(longSumData.getPoints()));
                sum.put("data_points", dataPoints);
                builder.put("sum", sum);
            }
            break;
            case DOUBLE_SUM: {
                DoubleSumData doubleSumData = metricData.getDoubleSumData();
                JSONObject doubleSum = new JSONObject();
                doubleSum.put("is_monotonic", doubleSumData.isMonotonic());
                doubleSum.put("aggregation_temporality",
                        mapToTemporality(doubleSumData.getAggregationTemporality()));
                JSONArray dataPoints = new JSONArray();
                dataPoints.addAll(toDoubleDataPoints(doubleSumData.getPoints()));
                doubleSum.put("data_points", dataPoints);
                builder.put("sum", doubleSum);
            }
            break;
            case SUMMARY: {
                DoubleSummaryData doubleSummaryData = metricData.getDoubleSummaryData();
                JSONObject doubleSummarySum = new JSONObject();
                JSONArray dataPoints = new JSONArray();
                dataPoints.addAll(toSummaryDataPoints(doubleSummaryData.getPoints()));
                doubleSummarySum.put("data_points", dataPoints);
                builder.put("summary", doubleSummarySum);
            }
            break;
            case LONG_GAUGE: {
                LongGaugeData longGaugeData = metricData.getLongGaugeData();
                JSONObject longGauge = new JSONObject();
                JSONArray gaugeDataPoints = new JSONArray();
                gaugeDataPoints.addAll(toIntDataPoints(longGaugeData.getPoints()));
                longGauge.put("data_points", gaugeDataPoints);
                builder.put("gauge", longGauge);
            }
            break;
            case DOUBLE_GAUGE: {
                DoubleGaugeData doubleGaugeData = metricData.getDoubleGaugeData();
                JSONObject doubleGauge = new JSONObject();
                JSONArray gaugeDataPoints = new JSONArray();
                gaugeDataPoints.addAll(toDoubleDataPoints(doubleGaugeData.getPoints()));
                doubleGauge.put("data_points", gaugeDataPoints);
                builder.put("gauge", doubleGauge);
            }
            break;
            case HISTOGRAM: {
                DoubleHistogramData doubleHistogramData = metricData.getDoubleHistogramData();
                JSONObject histogram = new JSONObject();
                histogram.put("aggregation_temporality",
                        mapToTemporality(doubleHistogramData.getAggregationTemporality()));
                JSONArray histogramDataPoints = new JSONArray();
                histogramDataPoints.addAll(toHistogramDataPoints(doubleHistogramData.getPoints()));
                histogram.put("data_points", histogramDataPoints);
                builder.put("histogram", histogram);
            }
            break;
        }
        return builder;
    }

    private static String mapToTemporality(AggregationTemporality temporality) {
        switch (temporality) {
            case CUMULATIVE:
                return "AGGREGATION_TEMPORALITY_CUMULATIVE";
            case DELTA:
                return "AGGREGATION_TEMPORALITY_DELTA";
        }
        return "AGGREGATION_TEMPORALITY_UNSPECIFIED";
    }

    // Fill labels too until Collector supports attributes and users have had a chance to update.
    @SuppressWarnings("deprecation")
    static JSONArray toIntDataPoints(Collection<LongPointData> points) {
        JSONArray result = new JSONArray(points.size());
        for (LongPointData longPoint : points) {
            JSONObject builder = new JSONObject();
            builder.put("start_time_unix_nano", longPoint.getStartEpochNanos());
            builder.put("time_unix_nano", longPoint.getEpochNanos());
            builder.put("as_int", longPoint.getValue());
            JSONArray attributes = new JSONArray();
            JSONArray labels = new JSONArray();
            fillAttributes(longPoint.getAttributes(), attributes, labels);
            builder.put("attributes", attributes);
            builder.put("labels", labels);
            JSONArray exemplars = new JSONArray();
            longPoint.getExemplars().forEach(e -> exemplars.add(toExemplar(e)));
            builder.put("exemplars", exemplars);
            result.add(builder);
        }
        return result;
    }

    // Fill labels too until Collector supports attributes and users have had a chance to update.
    @SuppressWarnings("deprecation")
    static JSONArray toDoubleDataPoints(Collection<DoublePointData> points) {
        JSONArray result = new JSONArray(points.size());
        for (DoublePointData doublePoint : points) {
            JSONObject builder = new JSONObject();
            builder.put("start_time_unix_nano", doublePoint.getStartEpochNanos());
            builder.put("time_unix_nano", doublePoint.getEpochNanos());
            builder.put("as_double", doublePoint.getValue());
            JSONArray attributes = new JSONArray();
            JSONArray labels = new JSONArray();
            fillAttributes(doublePoint.getAttributes(), attributes, labels);
            builder.put("attributes", attributes);
            builder.put("labels", labels);
            JSONArray exemplars = new JSONArray();
            doublePoint.getExemplars().forEach(e -> exemplars.add(toExemplar(e)));
            builder.put("exemplars", exemplars);
            result.add(builder);
        }
        return result;
    }

    // Fill labels too until Collector supports attributes and users have had a chance to update.
    @SuppressWarnings("deprecation")
    static JSONArray toSummaryDataPoints(Collection<DoubleSummaryPointData> points) {
        JSONArray result = new JSONArray(points.size());
        for (DoubleSummaryPointData doubleSummaryPoint : points) {
            JSONObject builder = new JSONObject();
            builder.put("start_time_unix_nano", doubleSummaryPoint.getStartEpochNanos());
            builder.put("time_unix_nano", doubleSummaryPoint.getEpochNanos());
            builder.put("count", doubleSummaryPoint.getCount());
            builder.put("sum", doubleSummaryPoint.getSum());
            JSONArray attributes = new JSONArray();
            JSONArray labels = new JSONArray();
            fillAttributes(
                    doubleSummaryPoint.getAttributes(), attributes, labels);
            builder.put("attributes", attributes);
            builder.put("labels", labels);
            // Not calling directly addAllQuantileValues because that generates couple of unnecessary
            // allocations if empty list.
            if (!doubleSummaryPoint.getPercentileValues().isEmpty()) {
                JSONArray quantileValues = new JSONArray();
                for (ValueAtPercentile valueAtPercentile : doubleSummaryPoint.getPercentileValues()) {
                    JSONObject value = new JSONObject();
                    value.put("quantile", valueAtPercentile.getPercentile() / 100.0);
                    value.put("value", valueAtPercentile.getValue());
                    quantileValues.add(value);
                }
                builder.put("quantile_values", quantileValues);
            }
            result.add(builder);
        }
        return result;
    }

    // Fill labels too until Collector supports attributes and users have had a chance to update.
    @SuppressWarnings("deprecation")
    static JSONArray toHistogramDataPoints(
            Collection<DoubleHistogramPointData> points) {
        JSONArray result = new JSONArray(points.size());
        for (DoubleHistogramPointData doubleHistogramPoint : points) {
            JSONObject builder = new JSONObject();
            builder.put("start_time_unix_nano", doubleHistogramPoint.getStartEpochNanos());
            builder.put("time_unix_nano", doubleHistogramPoint.getEpochNanos());
            builder.put("count", doubleHistogramPoint.getCount());
            builder.put("sum", doubleHistogramPoint.getSum());
            JSONArray bucketCounts = new JSONArray();
            bucketCounts.addAll(doubleHistogramPoint.getCounts());
            builder.put("bucket_counts", bucketCounts);

            List<Double> boundaries = doubleHistogramPoint.getBoundaries();
            if (!boundaries.isEmpty()) {
                builder.put("explicit_bounds", boundaries);
            }
            JSONArray attributes = new JSONArray();
            JSONArray labels = new JSONArray();
            fillAttributes(
                    doubleHistogramPoint.getAttributes(), attributes, labels);
            builder.put("attributes", attributes);
            builder.put("labels", labels);
            JSONArray exemplars = new JSONArray();
            doubleHistogramPoint.getExemplars().forEach(e -> exemplars.add(toExemplar(e)));
            builder.put("exemplars", exemplars);
            result.add(builder);
        }
        return result;
    }

    static JSONArray toIntSumDataPoints(Collection<LongPointData> points) {
        JSONArray result = new JSONArray(points.size());
        for (LongPointData longPoint : points) {
            JSONObject builder = new JSONObject();
            builder.put("start_time_unix_nano", longPoint.getStartEpochNanos());
            builder.put("time_unix_nano", longPoint.getEpochNanos());
            builder.put("value", longPoint.getValue());

            JSONArray labels = toProtoLabels(longPoint.getAttributes());
            JSONArray exemplars = new JSONArray();
            longPoint.getExemplars().forEach(e -> exemplars.add(toIntExemplar(e)));
            builder.put("exemplars", exemplars);
            if (!labels.isEmpty()) {
                builder.put("labels", labels);
            }
            result.add(builder);
        }
        return result;
    }

    static JSONArray toProtoLabels(Attributes attributes) {
        final JSONArray result = new JSONArray(attributes.size());
        attributes.forEach(
                (key, value) -> {
                    JSONObject stringKeyValue = new JSONObject();
                    stringKeyValue.put("key", key.getKey());
                    stringKeyValue.put("value", value.toString());
                    result.add(stringKeyValue);
                });
        return result;
    }

    static JSONObject toIntExemplar(Exemplar exemplar) {
        JSONObject builder = new JSONObject();
        builder.put("time_unix_nano", exemplar.getEpochNanos());
        if (exemplar.getSpanId() != null) {
            builder.put("span_id", exemplar.getSpanId());
        }
        if (exemplar.getTraceId() != null) {
            builder.put("trace_id", exemplar.getTraceId());
        }
        JSONArray filteredLabels = new JSONArray();
        fillIntSumAttributes(
                exemplar.getFilteredAttributes(),
                filteredLabels);
        builder.put("value", ((LongExemplar) exemplar).getValue());
        return builder;
    }

    private static void fillIntSumAttributes(
            Attributes attributes,
            JSONArray labelSetter) {
        attributes.forEach(
                (key, value) -> {
                    JSONObject stringKeyValue = new JSONObject();
                    stringKeyValue.put("key", key.getKey());
                    stringKeyValue.put("value", value.toString());
                    labelSetter.add(stringKeyValue);
                });
    }

    // Fill labels too until Collector supports attributes and users have had a chance to update.
    @SuppressWarnings("deprecation")
    static JSONObject toExemplar(Exemplar exemplar) {
        JSONObject builder = new JSONObject();
        builder.put("time_unix_nano", exemplar.getEpochNanos());
        if (exemplar.getSpanId() != null) {
            builder.put("span_id", exemplar.getSpanId());
        }
        if (exemplar.getTraceId() != null) {
            builder.put("trace_id", exemplar.getTraceId());
        }
        JSONArray filteredAttributes = new JSONArray();
        JSONArray filteredLabels = new JSONArray();
        fillAttributes(
                exemplar.getFilteredAttributes(),
                filteredAttributes,
                filteredLabels);
        builder.put("filtered_attributes", filteredAttributes);
        builder.put("filtered_labels", filteredLabels);
        if (exemplar instanceof LongExemplar) {
            builder.put("as_int", ((LongExemplar) exemplar).getValue());
        } else if (exemplar instanceof DoubleExemplar) {
            builder.put("as_double", ((DoubleExemplar) exemplar).getValue());
        } else {
            if (logger.isLoggable(Level.SEVERE)) {
                logger.log(Level.SEVERE, "Unable to convert unknown exemplar type: " + exemplar);
            }
        }
        return builder;
    }

    // Fill labels too until Collector supports attributes and users have had a chance to update.
    @SuppressWarnings("deprecation")
    private static void fillAttributes(
            Attributes attributes,
            JSONArray attributeSetter,
            JSONArray labelSetter) {
        attributes.forEach(
                (key, value) -> {
                    attributeSetter.add(JsonCommonAdapter.toJsonAttribute(key, value));
                    labelSetter.add(JsonCommonAdapter.toJsonAttribute(key, value));
                });
    }

    private JsonMetricAdapter() {}
}
