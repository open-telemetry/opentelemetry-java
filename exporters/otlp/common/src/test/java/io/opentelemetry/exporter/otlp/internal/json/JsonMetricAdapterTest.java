package io.opentelemetry.exporter.otlp.internal.json;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.ImmutableList;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.LongExemplar;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class JsonMetricAdapterTest {
    private static final Attributes KV_ATTR = Attributes.of(stringKey("k"), "v");

    private static JSONObject stringValue(String value) {
        JSONObject stringValue = new JSONObject();
        stringValue.put("string_value", value);
        return stringValue;
    }

    @Test
    void toInt64DataPoints() {
        assertThat(JsonMetricAdapter.toIntDataPoints(Collections.emptyList())).isEmpty();

        JSONObject numberDataPoint = new JSONObject();
        numberDataPoint.put("start_time_unix_nano", 123L);
        numberDataPoint.put("time_unix_nano", 456L);

        JSONArray attributes = new JSONArray();
        JSONObject attribute = new JSONObject();
        attribute.put("key", "k");
        attribute.put("value", stringValue("v"));
        attributes.add(attribute);
        numberDataPoint.put("attributes", attributes);

        JSONArray labels = new JSONArray();
        JSONObject label = new JSONObject();
        label.put("key", "k");
        label.put("value", stringValue("v"));
        labels.add(label);
        numberDataPoint.put("labels", labels);
        numberDataPoint.put("as_int", 5L);

        JSONArray exemplars = new JSONArray();
        JSONObject exemplar = new JSONObject();
        exemplar.put("time_unix_nano", 2);

        JSONArray filteredAttributes = new JSONArray();
        JSONObject filteredAttribute = new JSONObject();
        filteredAttribute.put("key", "test");
        filteredAttribute.put("value", stringValue("value"));
        filteredAttributes.add(filteredAttribute);
        exemplar.put("filtered_attributes", filteredAttributes);

        JSONArray filteredLabels = new JSONArray();
        JSONObject filteredLabel = new JSONObject();
        filteredLabel.put("key", "test");
        filteredLabel.put("value", stringValue("value"));
        filteredLabels.add(filteredLabel);
        exemplar.put("filtered_labels", filteredLabels);

        exemplar.put("span_id", "0000000000000002");
        exemplar.put("trace_id", "00000000000000000000000000000001");
        exemplar.put("as_int", 1L);
        exemplars.add(exemplar);

        numberDataPoint.put("exemplars", exemplars);

        JSONArray actualJsonArray = JsonMetricAdapter.toIntDataPoints(
                singletonList(
                        LongPointData.create(
                                123,
                                456,
                                KV_ATTR,
                                5,
                                Arrays.asList(
                                        LongExemplar.create(
                                                Attributes.of(stringKey("test"), "value"),
                                                2,
                                                /*spanId=*/ "0000000000000002",
                                                /*traceId=*/ "00000000000000000000000000000001",
                                                1)))));
        boolean containsExactly = JsonCompareUtil.containsExactly(actualJsonArray, numberDataPoint);
        assertThat(containsExactly).isTrue();

        JSONArray actualJsonArray2 = JsonMetricAdapter.toIntDataPoints(
                ImmutableList.of(
                        LongPointData.create(123, 456, Attributes.empty(), 5),
                        LongPointData.create(321, 654, KV_ATTR, 7)));

        JSONArray exceptedJsonArray2 = new JSONArray();
        JSONObject exceptedNumberDataPoint1 = new JSONObject();
        exceptedNumberDataPoint1.put("start_time_unix_nano", 123L);
        exceptedNumberDataPoint1.put("time_unix_nano", 456L);
        exceptedNumberDataPoint1.put("as_int", 5L);
        exceptedNumberDataPoint1.put("attributes", new JSONArray());
        exceptedNumberDataPoint1.put("labels", new JSONArray());
        exceptedJsonArray2.add(exceptedNumberDataPoint1);

        JSONObject exceptedNumberDataPoint2 = new JSONObject();
        exceptedNumberDataPoint2.put("start_time_unix_nano", 321L);
        exceptedNumberDataPoint2.put("time_unix_nano", 654L);
        exceptedNumberDataPoint2.put("as_int", 7L);

        JSONArray exceptedNumberDataPointAttributes = new JSONArray();
        JSONObject exceptedNumberDataPointAttribute = new JSONObject();
        exceptedNumberDataPointAttribute.put("key", "k");
        exceptedNumberDataPointAttribute.put("value", stringValue("v"));
        exceptedNumberDataPointAttributes.add(exceptedNumberDataPointAttribute);
        exceptedNumberDataPoint2.put("attributes", exceptedNumberDataPointAttributes);

        JSONArray exceptedNumberDataPointLabels = new JSONArray();
        JSONObject exceptedNumberDataPointLabel = new JSONObject();
        exceptedNumberDataPointLabel.put("key", "k");
        exceptedNumberDataPointLabel.put("value", stringValue("v"));
        exceptedNumberDataPointLabels.add(exceptedNumberDataPointLabel);
        exceptedNumberDataPoint2.put("labels", exceptedNumberDataPointLabels);
        exceptedJsonArray2.add(exceptedNumberDataPoint2);

        boolean containsExactly2 = JsonCompareUtil.containsExactly(actualJsonArray2, exceptedJsonArray2);
        assertThat(containsExactly2).isTrue();
    }

    @Test
    void toDoubleDataPoints() {
        assertThat(JsonMetricAdapter.toDoubleDataPoints(Collections.emptyList())).isEmpty();

        JSONArray actualJsonArray = JsonMetricAdapter.toDoubleDataPoints(
                singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1)));

        JSONObject exceptedJson = new JSONObject();
        exceptedJson.put("start_time_unix_nano", 123L);
        exceptedJson.put("time_unix_nano", 456L);
        exceptedJson.put("as_double", 5.1);

        JSONArray attributes = new JSONArray();
        JSONObject attribute = new JSONObject();
        attribute.put("key", "k");
        attribute.put("value", stringValue("v"));
        attributes.add(attribute);
        exceptedJson.put("attributes", attributes);

        JSONArray labels = new JSONArray();
        JSONObject label = new JSONObject();
        label.put("key", "k");
        label.put("value", stringValue("v"));
        labels.add(label);
        exceptedJson.put("labels", labels);
        exceptedJson.put("exemplars", new JSONArray());

        boolean containsExactly = JsonCompareUtil.containsExactly(actualJsonArray, exceptedJson);
        assertThat(containsExactly).isTrue();

        JSONArray actualJsonArray2 = JsonMetricAdapter.toDoubleDataPoints(
                        ImmutableList.of(
                                DoublePointData.create(123, 456, Attributes.empty(), 5.1),
                                DoublePointData.create(321, 654, KV_ATTR, 7.1)));

        JSONArray exceptedJsonArray2 = new JSONArray();
        JSONObject exceptedDoublePoint1 = new JSONObject();
        exceptedDoublePoint1.put("start_time_unix_nano", 123L);
        exceptedDoublePoint1.put("time_unix_nano", 456L);
        exceptedDoublePoint1.put("as_double", 5.1);
        exceptedDoublePoint1.put("attributes", new JSONArray());
        exceptedDoublePoint1.put("labels", new JSONArray());
        exceptedJsonArray2.add(exceptedDoublePoint1);

        JSONObject exceptedDoublePoint2 = new JSONObject();
        exceptedDoublePoint2.put("start_time_unix_nano", 321L);
        exceptedDoublePoint2.put("time_unix_nano", 654L);
        exceptedDoublePoint2.put("as_int", 7L);

        JSONArray exceptedDoublePointAttributes = new JSONArray();
        JSONObject exceptedDoublePointAttribute = new JSONObject();
        exceptedDoublePointAttribute.put("key", "k");
        exceptedDoublePointAttribute.put("value", stringValue("v"));
        exceptedDoublePointAttributes.add(exceptedDoublePointAttribute);
        exceptedDoublePoint2.put("attributes", exceptedDoublePointAttributes);

        JSONArray exceptedNumberDataPointLabels = new JSONArray();
        JSONObject exceptedNumberDataPointLabel = new JSONObject();
        exceptedNumberDataPointLabel.put("key", "k");
        exceptedNumberDataPointLabel.put("value", stringValue("v"));
        exceptedNumberDataPointLabels.add(exceptedNumberDataPointLabel);
        exceptedDoublePoint2.put("labels", exceptedNumberDataPointLabels);
        exceptedJsonArray2.add(exceptedDoublePoint2);

        boolean containsExactly2 = JsonCompareUtil.containsExactly(actualJsonArray2, exceptedJsonArray2);
        assertThat(containsExactly2).isTrue();
    }

    @Test
    void toSummaryDataPoints() {
        JSONArray actualJsonArray = JsonMetricAdapter.toSummaryDataPoints(
                singletonList(
                        DoubleSummaryPointData.create(
                                123,
                                456,
                                KV_ATTR,
                                5,
                                14.2,
                                singletonList(ValueAtPercentile.create(0.0, 1.1)))));

        JSONObject exceptedJson = new JSONObject();
        exceptedJson.put("start_time_unix_nano", 123L);
        exceptedJson.put("time_unix_nano", 456L);

        JSONArray attributes = new JSONArray();
        JSONObject attribute = new JSONObject();
        attribute.put("key", "k");
        attribute.put("value", stringValue("v"));
        attributes.add(attribute);
        exceptedJson.put("attributes", attributes);

        JSONArray labels = new JSONArray();
        JSONObject label = new JSONObject();
        label.put("key", "k");
        label.put("value", stringValue("v"));
        labels.add(label);
        exceptedJson.put("labels", labels);
        exceptedJson.put("count", 5L);
        exceptedJson.put("sum", 14.2);

        JSONArray quantileValues = new JSONArray();
        JSONObject quantileValue = new JSONObject();
        quantileValue.put("quantile", 0.0);
        quantileValue.put("value", 1.1);
        quantileValues.add(quantileValue);
        exceptedJson.put("quantile_values", quantileValues);

        boolean containsExactly = JsonCompareUtil.containsExactly(actualJsonArray, exceptedJson);
        assertThat(containsExactly).isTrue();

        JSONArray actualJsonArray2 = JsonMetricAdapter.toSummaryDataPoints(
                ImmutableList.of(
                        DoubleSummaryPointData.create(
                                123, 456, Attributes.empty(), 7, 15.3, Collections.emptyList()),
                        DoubleSummaryPointData.create(
                                321,
                                654,
                                KV_ATTR,
                                9,
                                18.3,
                                ImmutableList.of(
                                        ValueAtPercentile.create(0.0, 1.1),
                                        ValueAtPercentile.create(100.0, 20.3)))));

        JSONArray exceptedJsonArray2 = new JSONArray();
        JSONObject exceptedDoubleSummaryPoint1 = new JSONObject();
        exceptedDoubleSummaryPoint1.put("start_time_unix_nano", 123L);
        exceptedDoubleSummaryPoint1.put("time_unix_nano", 456L);
        exceptedDoubleSummaryPoint1.put("count", 7);
        exceptedDoubleSummaryPoint1.put("sum", 15.3);
        exceptedDoubleSummaryPoint1.put("attributes", new JSONArray());
        exceptedDoubleSummaryPoint1.put("labels", new JSONArray());
        exceptedJsonArray2.add(exceptedDoubleSummaryPoint1);

        JSONObject exceptedDoubleSummaryPoint2 = new JSONObject();
        exceptedDoubleSummaryPoint2.put("start_time_unix_nano", 321L);
        exceptedDoubleSummaryPoint2.put("time_unix_nano", 654L);

        JSONArray exceptedDoubleSummaryPointAttributes = new JSONArray();
        JSONObject exceptedDoubleSummaryPointAttribute = new JSONObject();
        exceptedDoubleSummaryPointAttribute.put("key", "k");
        exceptedDoubleSummaryPointAttribute.put("value", stringValue("v"));
        exceptedDoubleSummaryPointAttributes.add(exceptedDoubleSummaryPointAttribute);
        exceptedDoubleSummaryPoint2.put("attributes", exceptedDoubleSummaryPointAttributes);

        JSONArray exceptedDoubleSummaryPointLabels = new JSONArray();
        JSONObject exceptedDoubleSummaryPointLabel = new JSONObject();
        exceptedDoubleSummaryPointLabel.put("key", "k");
        exceptedDoubleSummaryPointLabel.put("value", stringValue("v"));
        exceptedDoubleSummaryPointLabels.add(exceptedDoubleSummaryPointLabel);
        exceptedDoubleSummaryPoint2.put("labels", exceptedDoubleSummaryPointLabels);

        exceptedDoubleSummaryPoint2.put("count", 9);
        exceptedDoubleSummaryPoint2.put("sum", 18.3);

        JSONArray exceptedDoubleSummaryPointQuantileValues = new JSONArray();
        JSONObject exceptedDoubleSummaryPointQuantileValue1 = new JSONObject();
        exceptedDoubleSummaryPointQuantileValue1.put("quantile", 0.0);
        exceptedDoubleSummaryPointQuantileValue1.put("value", 1.1);
        exceptedDoubleSummaryPointQuantileValues.add(exceptedDoubleSummaryPointQuantileValue1);
        JSONObject exceptedDoubleSummaryPointQuantileValue2 = new JSONObject();
        exceptedDoubleSummaryPointQuantileValue2.put("quantile", 100.0);
        exceptedDoubleSummaryPointQuantileValue2.put("value", 20.3);
        exceptedDoubleSummaryPointQuantileValues.add(exceptedDoubleSummaryPointQuantileValue2);
        exceptedDoubleSummaryPoint2.put("quantile_values", exceptedDoubleSummaryPointQuantileValues);
        exceptedJsonArray2.add(exceptedDoubleSummaryPoint2);

        boolean containsExactly2 = JsonCompareUtil.containsExactly(actualJsonArray2, exceptedJsonArray2);
        assertThat(containsExactly2).isTrue();
    }

    @Test
    void toHistogramDataPoints() {
        JSONArray actualJsonArray = JsonMetricAdapter.toHistogramDataPoints(
                ImmutableList.of(
                        DoubleHistogramPointData.create(
                                123, 456, KV_ATTR, 14.2, ImmutableList.of(1.0), ImmutableList.of(1L, 5L)),
                        DoubleHistogramPointData.create(
                                123,
                                456,
                                Attributes.empty(),
                                15.3,
                                ImmutableList.of(),
                                ImmutableList.of(7L),
                                ImmutableList.of(
                                        DoubleExemplar.create(
                                                Attributes.of(stringKey("test"), "value"),
                                                2,
                                                /*spanId=*/ "0000000000000002",
                                                /*traceId=*/ "00000000000000000000000000000001",
                                                1.5)))));

        JSONArray exceptedJsonArray = new JSONArray();
        JSONObject exceptedHistogramDataPoint1 = new JSONObject();
        exceptedHistogramDataPoint1.put("start_time_unix_nano", 123L);
        exceptedHistogramDataPoint1.put("time_unix_nano", 456L);

        JSONArray exceptedHistogramDataPointAttributes = new JSONArray();
        JSONObject exceptedHistogramDataPointAttribute = new JSONObject();
        exceptedHistogramDataPointAttribute.put("key", "k");
        exceptedHistogramDataPointAttribute.put("value", stringValue("v"));
        exceptedHistogramDataPointAttributes.add(exceptedHistogramDataPointAttribute);
        exceptedHistogramDataPoint1.put("attributes", exceptedHistogramDataPointAttributes);

        JSONArray exceptedHistogramDataPointLabels = new JSONArray();
        JSONObject exceptedHistogramDataPointLabel = new JSONObject();
        exceptedHistogramDataPointLabel.put("key", "k");
        exceptedHistogramDataPointLabel.put("value", stringValue("v"));
        exceptedHistogramDataPointLabels.add(exceptedHistogramDataPointLabel);
        exceptedHistogramDataPoint1.put("labels", exceptedHistogramDataPointLabels);

        exceptedHistogramDataPoint1.put("count", 6);
        exceptedHistogramDataPoint1.put("sum", 14.2);

        JSONArray bucketCounts = new JSONArray();
        bucketCounts.add(1);
        bucketCounts.add(5);
        exceptedHistogramDataPoint1.put("bucket_counts", bucketCounts);

        JSONArray explicitCounts = new JSONArray();
        explicitCounts.add(1);
        exceptedHistogramDataPoint1.put("explicit_bounds", explicitCounts);
        exceptedHistogramDataPoint1.put("exemplars", new JSONArray());
        exceptedJsonArray.add(exceptedHistogramDataPoint1);

        JSONObject exceptedHistogramDataPoint2 = new JSONObject();
        exceptedHistogramDataPoint2.put("start_time_unix_nano", 123L);
        exceptedHistogramDataPoint2.put("time_unix_nano", 456L);

        exceptedHistogramDataPoint2.put("attributes", new JSONArray());
        exceptedHistogramDataPoint2.put("labels", new JSONArray());

        exceptedHistogramDataPoint2.put("count", 7);
        exceptedHistogramDataPoint2.put("sum", 15.3);

        JSONArray bucketCounts2 = new JSONArray();
        bucketCounts2.add(7);
        exceptedHistogramDataPoint2.put("bucket_counts", bucketCounts2);

        JSONArray exemplars2 = new JSONArray();
        JSONObject exemplar = new JSONObject();
        exemplar.put("time_unix_nano", 2);

        JSONArray filteredAttributes = new JSONArray();
        JSONObject filteredAttribute = new JSONObject();
        filteredAttribute.put("key", "test");
        filteredAttribute.put("value", stringValue("value"));
        filteredAttributes.add(filteredAttribute);
        exemplar.put("filtered_attributes", filteredAttributes);

        JSONArray filteredLabels = new JSONArray();
        JSONObject filteredLabel = new JSONObject();
        filteredLabel.put("key", "test");
        filteredLabel.put("value", stringValue("value"));
        filteredLabels.add(filteredLabel);
        exemplar.put("filtered_labels", filteredLabels);

        exemplar.put("span_id", "0000000000000002");
        exemplar.put("trace_id", "00000000000000000000000000000001");
        exemplar.put("as_double", 1.5);
        exemplars2.add(exemplar);

        exceptedHistogramDataPoint2.put("exemplars", exemplars2);
        exceptedJsonArray.add(exceptedHistogramDataPoint2);

        boolean containsExactly = JsonCompareUtil.containsExactly(actualJsonArray, exceptedJsonArray);
        assertThat(containsExactly).isTrue();
    }

    @Test
    void toProtoMetric_monotonic() {
        JSONObject actualJson = JsonMetricAdapter.toProtoMetric(
                MetricData.createLongSum(
                        Resource.empty(),
                        InstrumentationLibraryInfo.empty(),
                        "name",
                        "description",
                        "1",
                        LongSumData.create(
                                /* isMonotonic= */ true,
                                AggregationTemporality.CUMULATIVE,
                                singletonList(LongPointData.create(123, 456, KV_ATTR, 5)))));

        JSONObject exceptedJson = new JSONObject();
        exceptedJson.put("name", "name");
        exceptedJson.put("description", "description");
        exceptedJson.put("unit", "1");
        JSONObject sum = new JSONObject();
        sum.put("is_monotonic", true);
        sum.put("aggregation_temporality", "AGGREGATION_TEMPORALITY_CUMULATIVE");

        JSONArray dataPoints = new JSONArray();
        JSONObject numberDataPoint = new JSONObject();
        numberDataPoint.put("start_time_unix_nano", 123L);
        numberDataPoint.put("time_unix_nano", 456L);

        JSONArray attributes = new JSONArray();
        JSONObject attribute = new JSONObject();
        attribute.put("key", "k");
        attribute.put("value", stringValue("v"));
        attributes.add(attribute);
        numberDataPoint.put("attributes", attributes);

        JSONArray labels = new JSONArray();
        JSONObject label = new JSONObject();
        label.put("key", "k");
        label.put("value", stringValue("v"));
        labels.add(label);
        numberDataPoint.put("labels", labels);
        numberDataPoint.put("as_int", 5L);
        numberDataPoint.put("exemplars", new JSONArray());
        dataPoints.add(numberDataPoint);
        sum.put("data_points", dataPoints);
        exceptedJson.put("sum", sum);

        assertThat(actualJson).isEqualTo(exceptedJson);

        JSONObject actualJson2 = JsonMetricAdapter.toProtoMetric(
                MetricData.createDoubleSum(
                        Resource.empty(),
                        InstrumentationLibraryInfo.empty(),
                        "name",
                        "description",
                        "1",
                        DoubleSumData.create(
                                /* isMonotonic= */ true,
                                AggregationTemporality.CUMULATIVE,
                                singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1)))));

        JSONObject exceptedJson2 = new JSONObject();
        exceptedJson2.put("name", "name");
        exceptedJson2.put("description", "description");
        exceptedJson2.put("unit", "1");
        JSONObject sum2 = new JSONObject();
        sum2.put("is_monotonic", true);
        sum2.put("aggregation_temporality", "AGGREGATION_TEMPORALITY_CUMULATIVE");

        JSONArray dataPoints2 = new JSONArray();
        JSONObject numberDataPoint2 = new JSONObject();
        numberDataPoint2.put("start_time_unix_nano", 123L);
        numberDataPoint2.put("time_unix_nano", 456L);

        JSONArray attributes2 = new JSONArray();
        JSONObject attribute2 = new JSONObject();
        attribute2.put("key", "k");
        attribute2.put("value", stringValue("v"));
        attributes2.add(attribute2);
        numberDataPoint2.put("attributes", attributes2);

        JSONArray labels2 = new JSONArray();
        JSONObject label2 = new JSONObject();
        label2.put("key", "k");
        label2.put("value", stringValue("v"));
        labels2.add(label2);
        numberDataPoint2.put("labels", labels2);
        numberDataPoint2.put("as_double", 5.1);
        numberDataPoint2.put("exemplars", new JSONArray());
        dataPoints2.add(numberDataPoint2);
        sum2.put("data_points", dataPoints2);
        exceptedJson2.put("sum", sum2);

        assertThat(actualJson2).isEqualTo(exceptedJson2);
    }

    @Test
    void toProtoMetric_nonMonotonic() {
        JSONObject actualJson = JsonMetricAdapter.toProtoMetric(
                MetricData.createLongSum(
                        Resource.empty(),
                        InstrumentationLibraryInfo.empty(),
                        "name",
                        "description",
                        "1",
                        LongSumData.create(
                                /* isMonotonic= */ false,
                                AggregationTemporality.CUMULATIVE,
                                singletonList(LongPointData.create(123, 456, KV_ATTR, 5)))));

        JSONObject exceptedJson = new JSONObject();
        exceptedJson.put("name", "name");
        exceptedJson.put("description", "description");
        exceptedJson.put("unit", "1");
        JSONObject sum = new JSONObject();
        sum.put("is_monotonic", false);
        sum.put("aggregation_temporality", "AGGREGATION_TEMPORALITY_CUMULATIVE");

        JSONArray dataPoints = new JSONArray();
        JSONObject numberDataPoint = new JSONObject();
        numberDataPoint.put("start_time_unix_nano", 123L);
        numberDataPoint.put("time_unix_nano", 456L);

        JSONArray attributes = new JSONArray();
        JSONObject attribute = new JSONObject();
        attribute.put("key", "k");
        attribute.put("value", stringValue("v"));
        attributes.add(attribute);
        numberDataPoint.put("attributes", attributes);

        JSONArray labels = new JSONArray();
        JSONObject label = new JSONObject();
        label.put("key", "k");
        label.put("value", stringValue("v"));
        labels.add(label);
        numberDataPoint.put("labels", labels);
        numberDataPoint.put("as_int", 5L);
        numberDataPoint.put("exemplars", new JSONArray());
        dataPoints.add(numberDataPoint);
        sum.put("data_points", dataPoints);
        exceptedJson.put("sum", sum);

        assertThat(actualJson).isEqualTo(exceptedJson);

        JSONObject actualJson2 = JsonMetricAdapter.toProtoMetric(
                MetricData.createDoubleSum(
                        Resource.empty(),
                        InstrumentationLibraryInfo.empty(),
                        "name",
                        "description",
                        "1",
                        DoubleSumData.create(
                                /* isMonotonic= */ false,
                                AggregationTemporality.CUMULATIVE,
                                singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1)))));

        JSONObject exceptedJson2 = new JSONObject();
        exceptedJson2.put("name", "name");
        exceptedJson2.put("description", "description");
        exceptedJson2.put("unit", "1");
        JSONObject sum2 = new JSONObject();
        sum2.put("is_monotonic", false);
        sum2.put("aggregation_temporality", "AGGREGATION_TEMPORALITY_CUMULATIVE");

        JSONArray dataPoints2 = new JSONArray();
        JSONObject numberDataPoint2 = new JSONObject();
        numberDataPoint2.put("start_time_unix_nano", 123L);
        numberDataPoint2.put("time_unix_nano", 456L);

        JSONArray attributes2 = new JSONArray();
        JSONObject attribute2 = new JSONObject();
        attribute2.put("key", "k");
        attribute2.put("value", stringValue("v"));
        attributes2.add(attribute2);
        numberDataPoint2.put("attributes", attributes2);

        JSONArray labels2 = new JSONArray();
        JSONObject label2 = new JSONObject();
        label2.put("key", "k");
        label2.put("value", stringValue("v"));
        labels2.add(label2);
        numberDataPoint2.put("labels", labels2);
        numberDataPoint2.put("as_double", 5.1);
        numberDataPoint2.put("exemplars", new JSONArray());
        dataPoints2.add(numberDataPoint2);
        sum2.put("data_points", dataPoints2);
        exceptedJson2.put("sum", sum2);

        assertThat(actualJson2).isEqualTo(exceptedJson2);
    }

    @Test
    void toProtoMetric_gauges() {
        JSONObject actualJson = JsonMetricAdapter.toProtoMetric(
                MetricData.createLongGauge(
                        Resource.empty(),
                        InstrumentationLibraryInfo.empty(),
                        "name",
                        "description",
                        "1",
                        LongGaugeData.create(
                                singletonList(LongPointData.create(123, 456, KV_ATTR, 5)))));

        JSONObject exceptedJson = new JSONObject();
        exceptedJson.put("name", "name");
        exceptedJson.put("description", "description");
        exceptedJson.put("unit", "1");

        JSONObject gauge = new JSONObject();
        JSONArray gaugeDataPoints = new JSONArray();
        JSONObject gaugeDataPoint = new JSONObject();
        gaugeDataPoint.put("start_time_unix_nano", 123L);
        gaugeDataPoint.put("time_unix_nano", 456L);

        JSONArray attributes = new JSONArray();
        JSONObject attribute = new JSONObject();
        attribute.put("key", "k");
        attribute.put("value", stringValue("v"));
        attributes.add(attribute);
        gaugeDataPoint.put("attributes", attributes);

        JSONArray labels = new JSONArray();
        JSONObject label = new JSONObject();
        label.put("key", "k");
        label.put("value", stringValue("v"));
        labels.add(label);
        gaugeDataPoint.put("labels", labels);
        gaugeDataPoint.put("as_int", 5L);
        gaugeDataPoint.put("exemplars", new JSONArray());
        gaugeDataPoints.add(gaugeDataPoint);
        gauge.put("data_points", gaugeDataPoints);
        exceptedJson.put("gauge", gauge);

        assertThat(actualJson).isEqualTo(exceptedJson);

        JSONObject actualJson2 = JsonMetricAdapter.toProtoMetric(
                MetricData.createDoubleGauge(
                        Resource.empty(),
                        InstrumentationLibraryInfo.empty(),
                        "name",
                        "description",
                        "1",
                        DoubleGaugeData.create(
                                singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1)))));

        JSONObject exceptedJson2 = new JSONObject();
        exceptedJson2.put("name", "name");
        exceptedJson2.put("description", "description");
        exceptedJson2.put("unit", "1");

        JSONObject gauge2 = new JSONObject();
        JSONArray gaugeDataPoints2 = new JSONArray();
        JSONObject gaugeDataPoint2 = new JSONObject();
        gaugeDataPoint2.put("start_time_unix_nano", 123L);
        gaugeDataPoint2.put("time_unix_nano", 456L);

        JSONArray attributes2 = new JSONArray();
        JSONObject attribute2 = new JSONObject();
        attribute2.put("key", "k");
        attribute2.put("value", stringValue("v"));
        attributes2.add(attribute2);
        gaugeDataPoint2.put("attributes", attributes2);

        JSONArray labels2 = new JSONArray();
        JSONObject label2 = new JSONObject();
        label2.put("key", "k");
        label2.put("value", stringValue("v"));
        labels2.add(label2);
        gaugeDataPoint2.put("labels", labels2);
        gaugeDataPoint2.put("as_double", 5.1);
        gaugeDataPoint2.put("exemplars", new JSONArray());
        gaugeDataPoints2.add(gaugeDataPoint2);
        gauge2.put("data_points", gaugeDataPoints2);
        exceptedJson2.put("gauge", gauge2);

        assertThat(actualJson2).isEqualTo(exceptedJson2);
    }

    @Test
    void toProtoMetric_summary() {
        JSONObject actualJson = JsonMetricAdapter.toProtoMetric(
                MetricData.createDoubleSummary(
                        Resource.empty(),
                        InstrumentationLibraryInfo.empty(),
                        "name",
                        "description",
                        "1",
                        DoubleSummaryData.create(
                                singletonList(
                                        DoubleSummaryPointData.create(
                                                123,
                                                456,
                                                KV_ATTR,
                                                5,
                                                33d,
                                                ImmutableList.of(
                                                        ValueAtPercentile.create(0, 1.1),
                                                        ValueAtPercentile.create(100.0, 20.3)))))));

        JSONObject exceptedJson = new JSONObject();
        exceptedJson.put("name", "name");
        exceptedJson.put("description", "description");
        exceptedJson.put("unit", "1");

        JSONObject summary = new JSONObject();
        JSONArray summaryDataPoints = new JSONArray();
        JSONObject summaryDataPoint = new JSONObject();
        summaryDataPoint.put("start_time_unix_nano", 123L);
        summaryDataPoint.put("time_unix_nano", 456L);

        JSONArray attributes = new JSONArray();
        JSONObject attribute = new JSONObject();
        attribute.put("key", "k");
        attribute.put("value", stringValue("v"));
        attributes.add(attribute);
        summaryDataPoint.put("attributes", attributes);

        JSONArray labels = new JSONArray();
        JSONObject label = new JSONObject();
        label.put("key", "k");
        label.put("value", stringValue("v"));
        labels.add(label);
        summaryDataPoint.put("labels", labels);
        summaryDataPoint.put("count", 5L);
        summaryDataPoint.put("sum", 33d);

        JSONArray quantileValues = new JSONArray();
        JSONObject quantileValue1 = new JSONObject();
        quantileValue1.put("quantile", 0 / 100.0);
        quantileValue1.put("value", 1.1);
        quantileValues.add(quantileValue1);

        JSONObject quantileValue2 = new JSONObject();
        quantileValue2.put("quantile", 100.0 / 100.0);
        quantileValue2.put("value", 20.3);
        quantileValues.add(quantileValue2);
        summaryDataPoint.put("quantile_values", quantileValues);

        summaryDataPoints.add(summaryDataPoint);
        summary.put("data_points", summaryDataPoints);
        exceptedJson.put("summary", summary);

        assertThat(actualJson).isEqualTo(exceptedJson);
    }

    @Test
    void toProtoMetric_histogram() {
        JSONObject actualJson = JsonMetricAdapter.toProtoMetric(
                MetricData.createDoubleHistogram(
                        Resource.empty(),
                        InstrumentationLibraryInfo.empty(),
                        "name",
                        "description",
                        "1",
                        DoubleHistogramData.create(
                                AggregationTemporality.DELTA,
                                singletonList(
                                        DoubleHistogramPointData.create(
                                                123,
                                                456,
                                                KV_ATTR,
                                                4.0,
                                                ImmutableList.of(),
                                                ImmutableList.of(33L))))));

        JSONObject exceptedJson = new JSONObject();
        exceptedJson.put("name", "name");
        exceptedJson.put("description", "description");
        exceptedJson.put("unit", "1");

        JSONObject histogram = new JSONObject();
        histogram.put("aggregation_temporality", "AGGREGATION_TEMPORALITY_DELTA");

        JSONArray histogramDataPoints = new JSONArray();
        JSONObject histogramDataPoint = new JSONObject();
        histogramDataPoint.put("start_time_unix_nano", 123L);
        histogramDataPoint.put("time_unix_nano", 456L);

        JSONArray attributes = new JSONArray();
        JSONObject attribute = new JSONObject();
        attribute.put("key", "k");
        attribute.put("value", stringValue("v"));
        attributes.add(attribute);
        histogramDataPoint.put("attributes", attributes);

        JSONArray labels = new JSONArray();
        JSONObject label = new JSONObject();
        label.put("key", "k");
        label.put("value", stringValue("v"));
        labels.add(label);
        histogramDataPoint.put("labels", labels);
        histogramDataPoint.put("count", 33L);
        histogramDataPoint.put("sum", 4.0);
        JSONArray bucketCounts = new JSONArray();
        bucketCounts.add(33L);
        histogramDataPoint.put("bucket_counts", bucketCounts);
        histogramDataPoint.put("exemplars", new JSONArray());

        histogramDataPoints.add(histogramDataPoint);
        histogram.put("data_points", histogramDataPoints);
        exceptedJson.put("histogram", histogram);

        assertThat(actualJson).isEqualTo(exceptedJson);
    }


    @Test
    void toProtoResourceMetrics() throws InterruptedException {
        JSONArray actualJsonArray = JsonMetricAdapter.toJsonResourceMetrics(
                ImmutableList.of(
                        MetricData.createDoubleSum(
                                Resource.create(Attributes.of(stringKey("ka"), "va"), "http://resource.url"),
                                InstrumentationLibraryInfo.create("name", "version", "http://url"),
                                "name",
                                "description",
                                "1",
                                DoubleSumData.create(
                                        /* isMonotonic= */ true,
                                        AggregationTemporality.CUMULATIVE,
                                        Collections.singletonList(
                                                DoublePointData.create(123, 456, KV_ATTR, 5.0)))),
                        MetricData.createDoubleSum(
                                Resource.create(Attributes.of(stringKey("ka"), "va"), "http://resource.url"),
                                InstrumentationLibraryInfo.create("name", "version", "http://url"),
                                "name",
                                "description",
                                "1",
                                DoubleSumData.create(
                                        /* isMonotonic= */ true,
                                        AggregationTemporality.CUMULATIVE,
                                        Collections.singletonList(
                                                DoublePointData.create(123, 456, KV_ATTR, 5.0)))),
                        MetricData.createDoubleSum(
                                Resource.empty(),
                                InstrumentationLibraryInfo.create("name", "version", "http://url"),
                                "name",
                                "description",
                                "1",
                                DoubleSumData.create(
                                        /* isMonotonic= */ true,
                                        AggregationTemporality.CUMULATIVE,
                                        Collections.singletonList(
                                                DoublePointData.create(123, 456, KV_ATTR, 5.0)))),
                        MetricData.createDoubleSum(
                                Resource.empty(),
                                InstrumentationLibraryInfo.empty(),
                                "name",
                                "description",
                                "1",
                                DoubleSumData.create(
                                        /* isMonotonic= */ true,
                                        AggregationTemporality.CUMULATIVE,
                                        Collections.singletonList(
                                                DoublePointData.create(123, 456, KV_ATTR, 5.0))))));

        JSONArray exceptedJsonArray = new JSONArray();
        JSONObject resourceMetrics = new JSONObject();
        JSONObject resourceJson = new JSONObject();
        JSONArray attributes = new JSONArray();
        JSONObject attribute = new JSONObject();
        attribute.put("key", "ka");
        attribute.put("value", stringValue("va"));
        attributes.add(attribute);
        resourceJson.put("attributes", attributes);
        resourceMetrics.put("resource", resourceJson);
        resourceMetrics.put("schema_url", "http://resource.url");

        JSONArray instrumentationLibraryMetrics = new JSONArray();
        JSONObject instrumentationLibraryMetric = new JSONObject();
        instrumentationLibraryMetric.put("instrumentation_library", getInstrumentationLibraryJson("name", "version"));
        JSONArray metrics = new JSONArray();
        metrics.addAll(ImmutableList.of(getMetricDoubleSum(), getMetricDoubleSum()));
        instrumentationLibraryMetric.put("metrics", metrics);
        instrumentationLibraryMetric.put("schema_url", "http://url");
        instrumentationLibraryMetrics.add(instrumentationLibraryMetric);
        resourceMetrics.put("instrumentation_library_metrics", instrumentationLibraryMetrics);
        exceptedJsonArray.add(resourceMetrics);

        JSONObject emptyResourceMetrics = new JSONObject();
        JSONObject emptyResourceAttributes = new JSONObject();
        emptyResourceAttributes.put("attributes", new JSONArray());
        emptyResourceMetrics.put("resource", emptyResourceAttributes);

        JSONArray instrumentationLibraryMetrics1 = new JSONArray();
        JSONObject emptyInstrumentationLibraryMetric1 = new JSONObject();
        emptyInstrumentationLibraryMetric1.put("instrumentation_library", getInstrumentationLibraryJson("name", "version"));
        JSONArray metrics1 = new JSONArray();
        metrics1.add(getMetricDoubleSum());
        emptyInstrumentationLibraryMetric1.put("metrics", metrics1);
        emptyInstrumentationLibraryMetric1.put("schema_url", "http://url");
        instrumentationLibraryMetrics1.add(emptyInstrumentationLibraryMetric1);

        JSONObject emptyInstrumentationLibraryMetric2 = new JSONObject();
        emptyInstrumentationLibraryMetric2.put("instrumentation_library", getInstrumentationLibraryJson("", null));
        JSONArray metrics2 = new JSONArray();
        metrics2.add(getMetricDoubleSum());
        emptyInstrumentationLibraryMetric2.put("metrics", metrics2);
        instrumentationLibraryMetrics1.add(emptyInstrumentationLibraryMetric2);
        emptyResourceMetrics.put("instrumentation_library_metrics", instrumentationLibraryMetrics1);
        exceptedJsonArray.add(emptyResourceMetrics);

        boolean containsExactly = false;
        for (int i = 0; i < exceptedJsonArray.size(); i++) {
            containsExactly = JsonCompareUtil.containsExactly(actualJsonArray, exceptedJsonArray.getJSONObject(i));
        }
        assertThat(containsExactly).isTrue();
    }

    private static JSONObject getInstrumentationLibraryJson(String name, String version) {
        JSONObject instrumentationLibraryJson = new JSONObject();
        instrumentationLibraryJson.put("name", name);
        if (version != null) {
            instrumentationLibraryJson.put("version", version);
        }
        return instrumentationLibraryJson;
    }

    private static JSONObject getMetricDoubleSum() {
        JSONObject metricDoubleSum = new JSONObject();
        metricDoubleSum.put("name", "name");
        metricDoubleSum.put("description", "description");
        metricDoubleSum.put("unit", "1");

        JSONObject sum = new JSONObject();
        sum.put("is_monotonic", true);
        sum.put("aggregation_temporality", "AGGREGATION_TEMPORALITY_CUMULATIVE");

        JSONArray dataPoints = new JSONArray();
        JSONObject numberDataPoint = new JSONObject();
        numberDataPoint.put("start_time_unix_nano", 123L);
        numberDataPoint.put("time_unix_nano", 456L);

        JSONArray numberDataPointAttributes = new JSONArray();
        JSONObject numberDataPointAttribute = new JSONObject();
        numberDataPointAttribute.put("key", "k");
        numberDataPointAttribute.put("value", stringValue("v"));
        numberDataPointAttributes.add(numberDataPointAttribute);
        numberDataPoint.put("attributes", numberDataPointAttributes);

        JSONArray numberDataPointLabels = new JSONArray();
        JSONObject numberDataPointLabel = new JSONObject();
        numberDataPointLabel.put("key", "k");
        numberDataPointLabel.put("value", stringValue("v"));
        numberDataPointLabels.add(numberDataPointLabel);
        numberDataPoint.put("labels", numberDataPointLabels);
        numberDataPoint.put("as_double", 5.0);
        numberDataPoint.put("exemplars", new JSONArray());
        dataPoints.add(numberDataPoint);
        sum.put("data_points", dataPoints);
        metricDoubleSum.put("sum", sum);

        return metricDoubleSum;
    }
}