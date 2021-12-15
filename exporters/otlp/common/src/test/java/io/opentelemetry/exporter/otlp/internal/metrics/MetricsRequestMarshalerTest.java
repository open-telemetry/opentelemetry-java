/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.OtelEncodingUtils;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationLibrary;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Exemplar;
import io.opentelemetry.proto.metrics.v1.ExponentialHistogram;
import io.opentelemetry.proto.metrics.v1.ExponentialHistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Gauge;
import io.opentelemetry.proto.metrics.v1.Histogram;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.proto.metrics.v1.Summary;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint;
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
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

// Fill deprecated APIs before removing them after users get a chance to migrate.
class MetricsRequestMarshalerTest {

  private static final Attributes KV_ATTR = Attributes.of(stringKey("k"), "v");

  private static AnyValue stringValue(String v) {
    return AnyValue.newBuilder().setStringValue(v).build();
  }

  @Test
  void dataPoint_withDefaultValues() {
    assertThat(
            toNumberDataPoints(
                singletonList(
                    LongPointData.create(
                        123,
                        456,
                        KV_ATTR,
                        0,
                        singletonList(
                            LongExemplarData.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                /*spanId=*/ "0000000000000002",
                                /*traceId=*/ "00000000000000000000000000000001",
                                0))))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsInt(0)
                .addExemplars(
                    Exemplar.newBuilder()
                        .setTimeUnixNano(2)
                        .addFilteredAttributes(
                            KeyValue.newBuilder()
                                .setKey("test")
                                .setValue(stringValue("value"))
                                .build())
                        .setSpanId(ByteString.copyFrom(new byte[] {0, 0, 0, 0, 0, 0, 0, 2}))
                        .setTraceId(
                            ByteString.copyFrom(
                                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}))
                        .setAsInt(0)
                        .build())
                .build());

    assertThat(
            toNumberDataPoints(
                singletonList(
                    DoublePointData.create(
                        123,
                        456,
                        KV_ATTR,
                        0,
                        singletonList(
                            DoubleExemplarData.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                /*spanId=*/ "0000000000000002",
                                /*traceId=*/ "00000000000000000000000000000001",
                                0))))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsDouble(0)
                .addExemplars(
                    Exemplar.newBuilder()
                        .setTimeUnixNano(2)
                        .addFilteredAttributes(
                            KeyValue.newBuilder()
                                .setKey("test")
                                .setValue(stringValue("value"))
                                .build())
                        .setSpanId(ByteString.copyFrom(new byte[] {0, 0, 0, 0, 0, 0, 0, 2}))
                        .setTraceId(
                            ByteString.copyFrom(
                                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}))
                        .setAsDouble(0)
                        .build())
                .build());
  }

  @Test
  void longDataPoints() {
    assertThat(toNumberDataPoints(Collections.emptyList())).isEmpty();
    assertThat(
            toNumberDataPoints(
                singletonList(
                    LongPointData.create(
                        123,
                        456,
                        KV_ATTR,
                        5,
                        singletonList(
                            LongExemplarData.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                /*spanId=*/ "0000000000000002",
                                /*traceId=*/ "00000000000000000000000000000001",
                                1))))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsInt(5)
                .addExemplars(
                    Exemplar.newBuilder()
                        .setTimeUnixNano(2)
                        .addFilteredAttributes(
                            KeyValue.newBuilder()
                                .setKey("test")
                                .setValue(stringValue("value"))
                                .build())
                        .setSpanId(ByteString.copyFrom(new byte[] {0, 0, 0, 0, 0, 0, 0, 2}))
                        .setTraceId(
                            ByteString.copyFrom(
                                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}))
                        .setAsInt(1)
                        .build())
                .build());
    assertThat(
            toNumberDataPoints(
                ImmutableList.of(
                    LongPointData.create(123, 456, Attributes.empty(), 5),
                    LongPointData.create(321, 654, KV_ATTR, 7))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setAsInt(5)
                .build(),
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsInt(7)
                .build());
  }

  @Test
  void doubleDataPoints() {
    assertThat(toNumberDataPoints(Collections.emptyList())).isEmpty();
    assertThat(toNumberDataPoints(singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsDouble(5.1)
                .build());
    assertThat(
            toNumberDataPoints(
                ImmutableList.of(
                    DoublePointData.create(123, 456, Attributes.empty(), 5.1),
                    DoublePointData.create(321, 654, KV_ATTR, 7.1))))
        .containsExactly(
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setAsDouble(5.1)
                .build(),
            NumberDataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setAsDouble(7.1)
                .build());
  }

  @Test
  void summaryDataPoints() {
    assertThat(
            toSummaryDataPoints(
                singletonList(
                    DoubleSummaryPointData.create(
                        123,
                        456,
                        KV_ATTR,
                        5,
                        14.2,
                        singletonList(ValueAtPercentile.create(0.0, 1.1))))))
        .containsExactly(
            SummaryDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setCount(5)
                .setSum(14.2)
                .addQuantileValues(
                    SummaryDataPoint.ValueAtQuantile.newBuilder()
                        .setQuantile(0.0)
                        .setValue(1.1)
                        .build())
                .build());
    assertThat(
            toSummaryDataPoints(
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
                            ValueAtPercentile.create(100.0, 20.3))))))
        .containsExactly(
            SummaryDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setCount(7)
                .setSum(15.3)
                .build(),
            SummaryDataPoint.newBuilder()
                .setStartTimeUnixNano(321)
                .setTimeUnixNano(654)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setCount(9)
                .setSum(18.3)
                .addQuantileValues(
                    SummaryDataPoint.ValueAtQuantile.newBuilder()
                        .setQuantile(0.0)
                        .setValue(1.1)
                        .build())
                .addQuantileValues(
                    SummaryDataPoint.ValueAtQuantile.newBuilder()
                        .setQuantile(1.0)
                        .setValue(20.3)
                        .build())
                .build());
  }

  @Test
  void histogramDataPoints() {
    assertThat(
            toHistogramDataPoints(
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
                            DoubleExemplarData.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                /*spanId=*/ "0000000000000002",
                                /*traceId=*/ "00000000000000000000000000000001",
                                1.5))))))
        .containsExactly(
            HistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("k").setValue(stringValue("v")).build()))
                .setCount(6)
                .setSum(14.2)
                .addBucketCounts(1)
                .addBucketCounts(5)
                .addExplicitBounds(1.0)
                .build(),
            HistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setCount(7)
                .setSum(15.3)
                .addBucketCounts(7)
                .addExemplars(
                    Exemplar.newBuilder()
                        .setTimeUnixNano(2)
                        .addFilteredAttributes(
                            KeyValue.newBuilder()
                                .setKey("test")
                                .setValue(stringValue("value"))
                                .build())
                        .setSpanId(ByteString.copyFrom(new byte[] {0, 0, 0, 0, 0, 0, 0, 2}))
                        .setTraceId(
                            ByteString.copyFrom(
                                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}))
                        .setAsDouble(1.5)
                        .build())
                .build());
  }

  @Test
  void exponentialHistogramDataPoints() {
    assertThat(
            toExponentialHistogramDataPoints(
                ImmutableList.of(
                    ExponentialHistogramPointData.create(
                        0,
                        123.4,
                        1,
                        new TestExponentialHistogramBuckets(1, ImmutableList.of(1L, 0L, 2L)),
                        new TestExponentialHistogramBuckets(0, Collections.emptyList()),
                        123,
                        456,
                        Attributes.of(stringKey("key"), "value"),
                        ImmutableList.of(
                            DoubleExemplarData.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                /*spanId=*/ "0000000000000002",
                                /*traceId=*/ "00000000000000000000000000000001",
                                1.5))))))
        .containsExactly(
            ExponentialHistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setCount(4) // Counts in positive, negative, and zero count.
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("key").setValue(stringValue("value")).build()))
                .setScale(0)
                .setSum(123.4)
                .setZeroCount(1)
                .setPositive(
                    ExponentialHistogramDataPoint.Buckets.newBuilder()
                        .setOffset(1)
                        .addBucketCounts(1)
                        .addBucketCounts(0)
                        .addBucketCounts(2))
                .setNegative(
                    ExponentialHistogramDataPoint.Buckets.newBuilder()
                        .setOffset(0)) // no buckets
                .addExemplars(
                    Exemplar.newBuilder()
                        .setTimeUnixNano(2)
                        .addFilteredAttributes(
                            KeyValue.newBuilder()
                                .setKey("test")
                                .setValue(stringValue("value"))
                                .build())
                        .setSpanId(ByteString.copyFrom(new byte[] {0, 0, 0, 0, 0, 0, 0, 2}))
                        .setTraceId(
                            ByteString.copyFrom(
                                new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1}))
                        .setAsDouble(1.5)
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_monotonic() {
    assertThat(
            toProtoMetric(
                MetricData.createLongSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    LongSumData.create(
                        /* isMonotonic= */ true,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(LongPointData.create(123, 456, KV_ATTR, 5))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setSum(
                    Sum.newBuilder()
                        .setIsMonotonic(true)
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsInt(5)
                                .build())
                        .build())
                .build());
    assertThat(
            toProtoMetric(
                MetricData.createDoubleSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleSumData.create(
                        /* isMonotonic= */ true,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setSum(
                    Sum.newBuilder()
                        .setIsMonotonic(true)
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsDouble(5.1)
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_nonMonotonic() {
    assertThat(
            toProtoMetric(
                MetricData.createLongSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    LongSumData.create(
                        /* isMonotonic= */ false,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(LongPointData.create(123, 456, KV_ATTR, 5))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setSum(
                    Sum.newBuilder()
                        .setIsMonotonic(false)
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsInt(5)
                                .build())
                        .build())
                .build());
    assertThat(
            toProtoMetric(
                MetricData.createDoubleSum(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleSumData.create(
                        /* isMonotonic= */ false,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setSum(
                    Sum.newBuilder()
                        .setIsMonotonic(false)
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsDouble(5.1)
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_gauges() {
    assertThat(
            toProtoMetric(
                MetricData.createLongGauge(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    LongGaugeData.create(
                        singletonList(LongPointData.create(123, 456, KV_ATTR, 5))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setGauge(
                    Gauge.newBuilder()
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsInt(5)
                                .build())
                        .build())
                .build());
    assertThat(
            toProtoMetric(
                MetricData.createDoubleGauge(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    DoubleGaugeData.create(
                        singletonList(DoublePointData.create(123, 456, KV_ATTR, 5.1))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setGauge(
                    Gauge.newBuilder()
                        .addDataPoints(
                            NumberDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setAsDouble(5.1)
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_summary() {
    assertThat(
            toProtoMetric(
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
                                    ValueAtPercentile.create(100.0, 20.3))))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setSummary(
                    Summary.newBuilder()
                        .addDataPoints(
                            SummaryDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setCount(5)
                                .setSum(33d)
                                .addQuantileValues(
                                    SummaryDataPoint.ValueAtQuantile.newBuilder()
                                        .setQuantile(0)
                                        .setValue(1.1)
                                        .build())
                                .addQuantileValues(
                                    SummaryDataPoint.ValueAtQuantile.newBuilder()
                                        .setQuantile(1.0)
                                        .setValue(20.3)
                                        .build())
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_histogram() {
    assertThat(
            toProtoMetric(
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
                                ImmutableList.of(33L)))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setHistogram(
                    Histogram.newBuilder()
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_DELTA)
                        .addDataPoints(
                            HistogramDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setCount(33)
                                .setSum(4.0)
                                .addBucketCounts(33)
                                .build())
                        .build())
                .build());
  }

  @Test
  void toProtoMetric_exponentialHistogram() {
    assertThat(
            toProtoMetric(
                MetricData.createExponentialHistogram(
                    Resource.empty(),
                    InstrumentationLibraryInfo.empty(),
                    "name",
                    "description",
                    "1",
                    ExponentialHistogramData.create(
                        AggregationTemporality.CUMULATIVE,
                        singletonList(
                            ExponentialHistogramPointData.create(
                                20,
                                123.4,
                                257,
                                new TestExponentialHistogramBuckets(
                                    -1, ImmutableList.of(0L, 128L, 1L << 32)),
                                new TestExponentialHistogramBuckets(
                                    1, ImmutableList.of(0L, 128L, 1L << 32)),
                                123,
                                456,
                                KV_ATTR,
                                ImmutableList.of()))))))
        .isEqualTo(
            Metric.newBuilder()
                .setName("name")
                .setDescription("description")
                .setUnit("1")
                .setExponentialHistogram(
                    ExponentialHistogram.newBuilder()
                        .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                        .addDataPoints(
                            ExponentialHistogramDataPoint.newBuilder()
                                .setStartTimeUnixNano(123)
                                .setTimeUnixNano(456)
                                .setCount(
                                    2 * (128L + (1L << 32))
                                        + 257L) // positive counts + negative counts + zero counts
                                .addAllAttributes(
                                    singletonList(
                                        KeyValue.newBuilder()
                                            .setKey("k")
                                            .setValue(stringValue("v"))
                                            .build()))
                                .setScale(20)
                                .setSum(123.4)
                                .setZeroCount(257)
                                .setPositive(
                                    ExponentialHistogramDataPoint.Buckets.newBuilder()
                                        .setOffset(-1)
                                        .addBucketCounts(0)
                                        .addBucketCounts(128)
                                        .addBucketCounts(1L << 32))
                                .setNegative(
                                    ExponentialHistogramDataPoint.Buckets.newBuilder()
                                        .setOffset(1)
                                        .addBucketCounts(0)
                                        .addBucketCounts(128)
                                        .addBucketCounts(1L << 32))))
                .build());
  }

  @Test
  void protoResourceMetrics() {
    Resource resource =
        Resource.create(Attributes.of(stringKey("ka"), "va"), "http://resource.url");
    io.opentelemetry.proto.resource.v1.Resource resourceProto =
        io.opentelemetry.proto.resource.v1.Resource.newBuilder()
            .addAllAttributes(
                singletonList(
                    KeyValue.newBuilder().setKey("ka").setValue(stringValue("va")).build()))
            .build();
    io.opentelemetry.proto.resource.v1.Resource emptyResourceProto =
        io.opentelemetry.proto.resource.v1.Resource.newBuilder().build();
    InstrumentationLibraryInfo instrumentationLibraryInfo =
        InstrumentationLibraryInfo.create("name", "version", "http://url");
    InstrumentationLibrary instrumentationLibraryProto =
        InstrumentationLibrary.newBuilder().setName("name").setVersion("version").build();
    InstrumentationLibrary emptyInstrumentationLibraryProto =
        InstrumentationLibrary.newBuilder().setName("").setVersion("").build();
    Metric metricDoubleSum =
        Metric.newBuilder()
            .setName("name")
            .setDescription("description")
            .setUnit("1")
            .setSum(
                Sum.newBuilder()
                    .setIsMonotonic(true)
                    .setAggregationTemporality(AGGREGATION_TEMPORALITY_CUMULATIVE)
                    .addDataPoints(
                        NumberDataPoint.newBuilder()
                            .setStartTimeUnixNano(123)
                            .setTimeUnixNano(456)
                            .addAllAttributes(
                                singletonList(
                                    KeyValue.newBuilder()
                                        .setKey("k")
                                        .setValue(stringValue("v"))
                                        .build()))
                            .setAsDouble(5.0)
                            .build())
                    .build())
            .build();

    assertThat(
            toProtoResourceMetrics(
                ImmutableList.of(
                    MetricData.createDoubleSum(
                        resource,
                        instrumentationLibraryInfo,
                        "name",
                        "description",
                        "1",
                        DoubleSumData.create(
                            /* isMonotonic= */ true,
                            AggregationTemporality.CUMULATIVE,
                            Collections.singletonList(
                                DoublePointData.create(123, 456, KV_ATTR, 5.0)))),
                    MetricData.createDoubleSum(
                        resource,
                        instrumentationLibraryInfo,
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
                        instrumentationLibraryInfo,
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
                                DoublePointData.create(123, 456, KV_ATTR, 5.0)))))))
        .satisfiesExactlyInAnyOrder(
            resourceMetrics -> {
              assertThat(resourceMetrics.getResource()).isEqualTo(resourceProto);
              assertThat(resourceMetrics.getSchemaUrl()).isEqualTo("http://resource.url");
              assertThat(resourceMetrics.getInstrumentationLibraryMetricsList())
                  .containsExactlyInAnyOrder(
                      InstrumentationLibraryMetrics.newBuilder()
                          .setInstrumentationLibrary(instrumentationLibraryProto)
                          .addAllMetrics(ImmutableList.of(metricDoubleSum, metricDoubleSum))
                          .setSchemaUrl("http://url")
                          .build());
            },
            resourceMetrics -> {
              assertThat(resourceMetrics.getResource()).isEqualTo(emptyResourceProto);
              assertThat(resourceMetrics.getInstrumentationLibraryMetricsList())
                  .containsExactlyInAnyOrder(
                      InstrumentationLibraryMetrics.newBuilder()
                          .setInstrumentationLibrary(emptyInstrumentationLibraryProto)
                          .addAllMetrics(singletonList(metricDoubleSum))
                          .build(),
                      InstrumentationLibraryMetrics.newBuilder()
                          .setInstrumentationLibrary(instrumentationLibraryProto)
                          .addAllMetrics(singletonList(metricDoubleSum))
                          .setSchemaUrl("http://url")
                          .build());
            });
  }

  private static List<NumberDataPoint> toNumberDataPoints(Collection<? extends PointData> points) {
    return points.stream()
        .map(
            point ->
                parse(NumberDataPoint.getDefaultInstance(), NumberDataPointMarshaler.create(point)))
        .collect(Collectors.toList());
  }

  private static List<SummaryDataPoint> toSummaryDataPoints(
      Collection<DoubleSummaryPointData> points) {
    return points.stream()
        .map(
            point ->
                parse(
                    SummaryDataPoint.getDefaultInstance(), SummaryDataPointMarshaler.create(point)))
        .collect(Collectors.toList());
  }

  private static List<HistogramDataPoint> toHistogramDataPoints(
      Collection<DoubleHistogramPointData> points) {
    return points.stream()
        .map(
            point ->
                parse(
                    HistogramDataPoint.getDefaultInstance(),
                    HistogramDataPointMarshaler.create(point)))
        .collect(Collectors.toList());
  }

  private static List<ExponentialHistogramDataPoint> toExponentialHistogramDataPoints(
      Collection<ExponentialHistogramPointData> points) {
    return points.stream()
        .map(
            point ->
                parse(
                    ExponentialHistogramDataPoint.getDefaultInstance(),
                    ExponentialHistogramDataPointMarshaler.create(point)))
        .collect(Collectors.toList());
  }

  private static Metric toProtoMetric(MetricData metricData) {
    return parse(Metric.getDefaultInstance(), MetricMarshaler.create(metricData));
  }

  private static List<ResourceMetrics> toProtoResourceMetrics(
      Collection<MetricData> metricDataList) {
    ExportMetricsServiceRequest exportRequest =
        parse(
            ExportMetricsServiceRequest.getDefaultInstance(),
            MetricsRequestMarshaler.create(metricDataList));
    return exportRequest.getResourceMetricsList();
  }

  @SuppressWarnings("unchecked")
  private static <T extends Message> T parse(T prototype, Marshaler marshaler) {
    byte[] serialized = toByteArray(marshaler);
    T result;
    try {
      result = (T) prototype.newBuilderForType().mergeFrom(serialized).build();
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }
    // Our marshaler should produce the exact same length of serialized output (for example, field
    // default values are not outputted), so we check that here. The output itself may have slightly
    // different ordering, mostly due to the way we don't output oneof values in field order all the
    // tieme. If the lengths are equal and the resulting protos are equal, the marshaling is
    // guaranteed to be valid.
    assertThat(result.getSerializedSize()).isEqualTo(serialized.length);

    // We don't compare JSON strings due to some differences (particularly serializing enums as
    // numbers instead of names). This may improve in the future but what matters is what we produce
    // can be parsed.
    String json = toJson(marshaler);
    Message.Builder builder = prototype.newBuilderForType();
    try {
      JsonFormat.parser().merge(json, builder);
    } catch (InvalidProtocolBufferException e) {
      throw new UncheckedIOException(e);
    }

    // Hackily swap out "hex as base64" decoded IDs with correct ones since no JSON protobuf
    // libraries currently support customizing on the parse side.
    if (result instanceof NumberDataPoint) {
      NumberDataPoint.Builder fixed = (NumberDataPoint.Builder) builder;
      for (Exemplar.Builder exemplar : fixed.getExemplarsBuilderList()) {
        exemplar.setTraceId(toHex(exemplar.getTraceId()));
        exemplar.setSpanId(toHex(exemplar.getSpanId()));
      }
    }
    if (result instanceof HistogramDataPoint) {
      HistogramDataPoint.Builder fixed = (HistogramDataPoint.Builder) builder;
      for (Exemplar.Builder exemplar : fixed.getExemplarsBuilderList()) {
        exemplar.setTraceId(toHex(exemplar.getTraceId()));
        exemplar.setSpanId(toHex(exemplar.getSpanId()));
      }
    }
    if (result instanceof ExponentialHistogramDataPoint) {
      ExponentialHistogramDataPoint.Builder fixed = (ExponentialHistogramDataPoint.Builder) builder;
      for (Exemplar.Builder exemplar : fixed.getExemplarsBuilderList()) {
        exemplar.setTraceId(toHex(exemplar.getTraceId()));
        exemplar.setSpanId(toHex(exemplar.getSpanId()));
      }
    }

    assertThat(builder.build()).isEqualTo(result);

    return result;
  }

  private static ByteString toHex(ByteString hexReadAsBase64) {
    String hex =
        Base64.getEncoder().encodeToString(hexReadAsBase64.toByteArray()).toLowerCase(Locale.ROOT);
    return ByteString.copyFrom(OtelEncodingUtils.bytesFromBase16(hex, hex.length()));
  }

  private static byte[] toByteArray(Marshaler marshaler) {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeBinaryTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return bos.toByteArray();
  }

  private static String toJson(Marshaler marshaler) {

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try {
      marshaler.writeJsonTo(bos);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return new String(bos.toByteArray(), StandardCharsets.UTF_8);
  }

  /**
   * Helper class for creating Exponential Histogram bucket data directly without needing to record.
   * Essentially, mocking out the bucket operations and downscaling.
   */
  private static class TestExponentialHistogramBuckets implements ExponentialHistogramBuckets {

    private final int offset;
    private final List<Long> bucketCounts;

    TestExponentialHistogramBuckets(int offset, List<Long> bucketCounts) {
      this.offset = offset;
      this.bucketCounts = bucketCounts;
    }

    @Override
    public int getOffset() {
      return offset;
    }

    @Override
    public List<Long> getBucketCounts() {
      return bucketCounts;
    }

    @Override
    public long getTotalCount() {
      return getBucketCounts().stream().reduce(0L, Long::sum);
    }
  }
}
