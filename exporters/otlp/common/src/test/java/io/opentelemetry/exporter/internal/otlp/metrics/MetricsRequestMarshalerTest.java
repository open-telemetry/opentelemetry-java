/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

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
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.InstrumentationScope;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.Exemplar;
import io.opentelemetry.proto.metrics.v1.ExponentialHistogram;
import io.opentelemetry.proto.metrics.v1.ExponentialHistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Gauge;
import io.opentelemetry.proto.metrics.v1.Histogram;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.ScopeMetrics;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.proto.metrics.v1.Summary;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.internal.DynamicPrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongExemplarData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableValueAtQuantile;
import io.opentelemetry.sdk.metrics.internal.data.MutableExponentialHistogramBuckets;
import io.opentelemetry.sdk.metrics.internal.data.MutableExponentialHistogramPointData;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

// Fill deprecated APIs before removing them after users get a chance to migrate.
class MetricsRequestMarshalerTest {

  private static final Attributes KV_ATTR = Attributes.of(stringKey("k"), "v");

  private static AnyValue stringValue(String v) {
    return AnyValue.newBuilder().setStringValue(v).build();
  }

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void dataPoint_withDefaultValues(MarshalerSource marshalerSource) {
    assertThat(
            toNumberDataPoints(
                marshalerSource,
                singletonList(
                    ImmutableLongPointData.create(
                        123,
                        456,
                        KV_ATTR,
                        0,
                        singletonList(
                            ImmutableLongExemplarData.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                SpanContext.create(
                                    "00000000000000000000000000000001",
                                    "0000000000000002",
                                    TraceFlags.getDefault(),
                                    TraceState.getDefault()),
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
                marshalerSource,
                singletonList(
                    ImmutableDoublePointData.create(
                        123,
                        456,
                        KV_ATTR,
                        0,
                        singletonList(
                            ImmutableDoubleExemplarData.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                SpanContext.create(
                                    "00000000000000000000000000000001",
                                    "0000000000000002",
                                    TraceFlags.getDefault(),
                                    TraceState.getDefault()),
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void longDataPoints(MarshalerSource marshalerSource) {
    assertThat(toNumberDataPoints(marshalerSource, Collections.emptyList())).isEmpty();
    assertThat(
            toNumberDataPoints(
                marshalerSource,
                singletonList(
                    ImmutableLongPointData.create(
                        123,
                        456,
                        KV_ATTR,
                        5,
                        singletonList(
                            ImmutableLongExemplarData.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                SpanContext.create(
                                    "00000000000000000000000000000001",
                                    "0000000000000002",
                                    TraceFlags.getDefault(),
                                    TraceState.getDefault()),
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
                marshalerSource,
                ImmutableList.of(
                    ImmutableLongPointData.create(123, 456, Attributes.empty(), 5),
                    ImmutableLongPointData.create(321, 654, KV_ATTR, 7))))
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void doubleDataPoints(MarshalerSource marshalerSource) {
    assertThat(toNumberDataPoints(marshalerSource, Collections.emptyList())).isEmpty();
    assertThat(
            toNumberDataPoints(
                marshalerSource,
                singletonList(ImmutableDoublePointData.create(123, 456, KV_ATTR, 5.1))))
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
                marshalerSource,
                ImmutableList.of(
                    ImmutableDoublePointData.create(123, 456, Attributes.empty(), 5.1),
                    ImmutableDoublePointData.create(321, 654, KV_ATTR, 7.1))))
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void summaryDataPoints(MarshalerSource marshalerSource) {
    assertThat(
            toSummaryDataPoints(
                marshalerSource,
                singletonList(
                    ImmutableSummaryPointData.create(
                        123,
                        456,
                        KV_ATTR,
                        5,
                        14.2,
                        singletonList(ImmutableValueAtQuantile.create(0.0, 1.1))))))
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
                marshalerSource,
                ImmutableList.of(
                    ImmutableSummaryPointData.create(
                        123, 456, Attributes.empty(), 7, 15.3, Collections.emptyList()),
                    ImmutableSummaryPointData.create(
                        321,
                        654,
                        KV_ATTR,
                        9,
                        18.3,
                        ImmutableList.of(
                            ImmutableValueAtQuantile.create(0.0, 1.1),
                            ImmutableValueAtQuantile.create(1.0, 20.3))))))
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void histogramDataPoints(MarshalerSource marshalerSource) {
    assertThat(
            toHistogramDataPoints(
                marshalerSource,
                ImmutableList.of(
                    ImmutableHistogramPointData.create(
                        123,
                        456,
                        KV_ATTR,
                        14.2,
                        /* hasMin= */ false,
                        0,
                        /* hasMax= */ false,
                        0,
                        ImmutableList.of(1.0),
                        ImmutableList.of(1L, 5L)),
                    ImmutableHistogramPointData.create(
                        123,
                        456,
                        Attributes.empty(),
                        15.3,
                        /* hasMin= */ true,
                        3.3,
                        /* hasMax= */ true,
                        12.0,
                        ImmutableList.of(),
                        ImmutableList.of(7L),
                        ImmutableList.of(
                            ImmutableDoubleExemplarData.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                SpanContext.create(
                                    "00000000000000000000000000000001",
                                    "0000000000000002",
                                    TraceFlags.getDefault(),
                                    TraceState.getDefault()),
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
                .setMin(3.3)
                .setMax(12.0)
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void exponentialHistogramDataPoints(MarshalerSource marshalerSource) {
    assertThat(
            toExponentialHistogramDataPoints(
                marshalerSource,
                ImmutableList.of(
                    ImmutableExponentialHistogramPointData.create(
                        0,
                        123.4,
                        1,
                        /* hasMin= */ false,
                        0,
                        /* hasMax= */ false,
                        0,
                        ImmutableExponentialHistogramBuckets.create(0, 0, Collections.emptyList()),
                        ImmutableExponentialHistogramBuckets.create(0, 0, Collections.emptyList()),
                        123,
                        456,
                        Attributes.empty(),
                        Collections.emptyList()),
                    ImmutableExponentialHistogramPointData.create(
                        0,
                        123.4,
                        1,
                        /* hasMin= */ true,
                        3.3,
                        /* hasMax= */ true,
                        80.1,
                        ImmutableExponentialHistogramBuckets.create(
                            0, 1, ImmutableList.of(1L, 0L, 2L)),
                        ImmutableExponentialHistogramBuckets.create(0, 0, Collections.emptyList()),
                        123,
                        456,
                        Attributes.of(stringKey("key"), "value"),
                        ImmutableList.of(
                            ImmutableDoubleExemplarData.create(
                                Attributes.of(stringKey("test"), "value"),
                                2,
                                SpanContext.create(
                                    "00000000000000000000000000000001",
                                    "0000000000000002",
                                    TraceFlags.getDefault(),
                                    TraceState.getDefault()),
                                1.5))))))
        .containsExactly(
            ExponentialHistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setCount(1)
                .setScale(0)
                .setSum(123.4)
                .setZeroCount(1)
                .setPositive(
                    ExponentialHistogramDataPoint.Buckets.newBuilder().setOffset(0)) // no buckets
                .setNegative(
                    ExponentialHistogramDataPoint.Buckets.newBuilder().setOffset(0)) // no buckets
                .build(),
            ExponentialHistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setCount(4) // Counts in positive, negative, and zero count.
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("key").setValue(stringValue("value")).build()))
                .setScale(0)
                .setSum(123.4)
                .setMin(3.3)
                .setMax(80.1)
                .setZeroCount(1)
                .setPositive(
                    ExponentialHistogramDataPoint.Buckets.newBuilder()
                        .setOffset(1)
                        .addBucketCounts(1)
                        .addBucketCounts(0)
                        .addBucketCounts(2))
                .setNegative(
                    ExponentialHistogramDataPoint.Buckets.newBuilder().setOffset(0)) // no buckets
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

  @SuppressWarnings("PointlessArithmeticExpression")
  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void exponentialHistogramReusableDataPoints(MarshalerSource marshalerSource) {
    assertThat(
            toExponentialHistogramDataPoints(
                marshalerSource,
                ImmutableList.of(
                    new MutableExponentialHistogramPointData()
                        .set(
                            0,
                            123.4,
                            1,
                            /* hasMin= */ false,
                            0,
                            /* hasMax= */ false,
                            0,
                            new MutableExponentialHistogramBuckets()
                                .set(0, 0, 0, DynamicPrimitiveLongList.empty()),
                            new MutableExponentialHistogramBuckets()
                                .set(0, 0, 0, DynamicPrimitiveLongList.empty()),
                            123,
                            456,
                            Attributes.empty(),
                            Collections.emptyList()),
                    new MutableExponentialHistogramPointData()
                        .set(
                            0,
                            123.4,
                            1,
                            /* hasMin= */ true,
                            3.3,
                            /* hasMax= */ true,
                            80.1,
                            new MutableExponentialHistogramBuckets()
                                .set(0, 1, 1 + 0 + 2, DynamicPrimitiveLongList.of(1L, 0L, 2L)),
                            new MutableExponentialHistogramBuckets()
                                .set(0, 0, 0, DynamicPrimitiveLongList.empty()),
                            123,
                            456,
                            Attributes.of(stringKey("key"), "value"),
                            ImmutableList.of(
                                ImmutableDoubleExemplarData.create(
                                    Attributes.of(stringKey("test"), "value"),
                                    2,
                                    SpanContext.create(
                                        "00000000000000000000000000000001",
                                        "0000000000000002",
                                        TraceFlags.getDefault(),
                                        TraceState.getDefault()),
                                    1.5))))))
        .containsExactly(
            ExponentialHistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setCount(1)
                .setScale(0)
                .setSum(123.4)
                .setZeroCount(1)
                .setPositive(
                    ExponentialHistogramDataPoint.Buckets.newBuilder().setOffset(0)) // no buckets
                .setNegative(
                    ExponentialHistogramDataPoint.Buckets.newBuilder().setOffset(0)) // no buckets
                .build(),
            ExponentialHistogramDataPoint.newBuilder()
                .setStartTimeUnixNano(123)
                .setTimeUnixNano(456)
                .setCount(4) // Counts in positive, negative, and zero count.
                .addAllAttributes(
                    singletonList(
                        KeyValue.newBuilder().setKey("key").setValue(stringValue("value")).build()))
                .setScale(0)
                .setSum(123.4)
                .setMin(3.3)
                .setMax(80.1)
                .setZeroCount(1)
                .setPositive(
                    ExponentialHistogramDataPoint.Buckets.newBuilder()
                        .setOffset(1)
                        .addBucketCounts(1)
                        .addBucketCounts(0)
                        .addBucketCounts(2))
                .setNegative(
                    ExponentialHistogramDataPoint.Buckets.newBuilder().setOffset(0)) // no buckets
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoMetric_monotonic(MarshalerSource marshalerSource) {
    assertThat(
            toProtoMetric(
                marshalerSource,
                ImmutableMetricData.createLongSum(
                    Resource.empty(),
                    InstrumentationScopeInfo.empty(),
                    "name",
                    "description",
                    "1",
                    ImmutableSumData.create(
                        /* isMonotonic= */ true,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(ImmutableLongPointData.create(123, 456, KV_ATTR, 5))))))
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
                marshalerSource,
                ImmutableMetricData.createDoubleSum(
                    Resource.empty(),
                    InstrumentationScopeInfo.empty(),
                    "name",
                    "description",
                    "1",
                    ImmutableSumData.create(
                        /* isMonotonic= */ true,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(ImmutableDoublePointData.create(123, 456, KV_ATTR, 5.1))))))
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoMetric_nonMonotonic(MarshalerSource marshalerSource) {
    assertThat(
            toProtoMetric(
                marshalerSource,
                ImmutableMetricData.createLongSum(
                    Resource.empty(),
                    InstrumentationScopeInfo.empty(),
                    "name",
                    "description",
                    "1",
                    ImmutableSumData.create(
                        /* isMonotonic= */ false,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(ImmutableLongPointData.create(123, 456, KV_ATTR, 5))))))
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
                marshalerSource,
                ImmutableMetricData.createDoubleSum(
                    Resource.empty(),
                    InstrumentationScopeInfo.empty(),
                    "name",
                    "description",
                    "1",
                    ImmutableSumData.create(
                        /* isMonotonic= */ false,
                        AggregationTemporality.CUMULATIVE,
                        singletonList(ImmutableDoublePointData.create(123, 456, KV_ATTR, 5.1))))))
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoMetric_gauges(MarshalerSource marshalerSource) {
    assertThat(
            toProtoMetric(
                marshalerSource,
                ImmutableMetricData.createLongGauge(
                    Resource.empty(),
                    InstrumentationScopeInfo.empty(),
                    "name",
                    "description",
                    "1",
                    ImmutableGaugeData.create(
                        singletonList(ImmutableLongPointData.create(123, 456, KV_ATTR, 5))))))
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
                marshalerSource,
                ImmutableMetricData.createDoubleGauge(
                    Resource.empty(),
                    InstrumentationScopeInfo.empty(),
                    "name",
                    "description",
                    "1",
                    ImmutableGaugeData.create(
                        singletonList(ImmutableDoublePointData.create(123, 456, KV_ATTR, 5.1))))))
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoMetric_summary(MarshalerSource marshalerSource) {
    assertThat(
            toProtoMetric(
                marshalerSource,
                ImmutableMetricData.createDoubleSummary(
                    Resource.empty(),
                    InstrumentationScopeInfo.empty(),
                    "name",
                    "description",
                    "1",
                    ImmutableSummaryData.create(
                        singletonList(
                            ImmutableSummaryPointData.create(
                                123,
                                456,
                                KV_ATTR,
                                5,
                                33d,
                                ImmutableList.of(
                                    ImmutableValueAtQuantile.create(0, 1.1),
                                    ImmutableValueAtQuantile.create(1.0, 20.3))))))))
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoMetric_histogram(MarshalerSource marshalerSource) {
    assertThat(
            toProtoMetric(
                marshalerSource,
                ImmutableMetricData.createDoubleHistogram(
                    Resource.empty(),
                    InstrumentationScopeInfo.empty(),
                    "name",
                    "description",
                    "1",
                    ImmutableHistogramData.create(
                        AggregationTemporality.DELTA,
                        singletonList(
                            ImmutableHistogramPointData.create(
                                123,
                                456,
                                KV_ATTR,
                                4.0,
                                /* hasMin= */ true,
                                1.0,
                                /* hasMax= */ true,
                                3.0,
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
                                .setMin(1.0)
                                .setMax(3.0)
                                .addBucketCounts(33)
                                .build())
                        .build())
                .build());
  }

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void toProtoMetric_exponentialHistogram(MarshalerSource marshalerSource) {
    assertThat(
            toProtoMetric(
                marshalerSource,
                ImmutableMetricData.createExponentialHistogram(
                    Resource.empty(),
                    InstrumentationScopeInfo.empty(),
                    "name",
                    "description",
                    "1",
                    ImmutableExponentialHistogramData.create(
                        AggregationTemporality.CUMULATIVE,
                        singletonList(
                            ImmutableExponentialHistogramPointData.create(
                                20,
                                123.4,
                                257,
                                /* hasMin= */ true,
                                20.1,
                                /* hasMax= */ true,
                                44.3,
                                ImmutableExponentialHistogramBuckets.create(
                                    20, -1, ImmutableList.of(0L, 128L, 1L << 32)),
                                ImmutableExponentialHistogramBuckets.create(
                                    20, 1, ImmutableList.of(0L, 128L, 1L << 32)),
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
                                .setMin(20.1)
                                .setMax(44.3)
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

  @ParameterizedTest
  @EnumSource(MarshalerSource.class)
  void protoResourceMetrics(MarshalerSource marshalerSource) {
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
    InstrumentationScopeInfo instrumentationScopeInfo =
        InstrumentationScopeInfo.builder("name")
            .setVersion("version")
            .setSchemaUrl("http://url")
            .setAttributes(Attributes.builder().put("key", "value").build())
            .build();
    InstrumentationScope scopeProto =
        InstrumentationScope.newBuilder()
            .setName("name")
            .setVersion("version")
            .addAttributes(
                KeyValue.newBuilder()
                    .setKey("key")
                    .setValue(AnyValue.newBuilder().setStringValue("value").build())
                    .build())
            .build();
    InstrumentationScope emptyScopeProto =
        InstrumentationScope.newBuilder().setName("").setVersion("").build();
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
                marshalerSource,
                ImmutableList.of(
                    ImmutableMetricData.createDoubleSum(
                        resource,
                        instrumentationScopeInfo,
                        "name",
                        "description",
                        "1",
                        ImmutableSumData.create(
                            /* isMonotonic= */ true,
                            AggregationTemporality.CUMULATIVE,
                            Collections.singletonList(
                                ImmutableDoublePointData.create(123, 456, KV_ATTR, 5.0)))),
                    ImmutableMetricData.createDoubleSum(
                        resource,
                        instrumentationScopeInfo,
                        "name",
                        "description",
                        "1",
                        ImmutableSumData.create(
                            /* isMonotonic= */ true,
                            AggregationTemporality.CUMULATIVE,
                            Collections.singletonList(
                                ImmutableDoublePointData.create(123, 456, KV_ATTR, 5.0)))),
                    ImmutableMetricData.createDoubleSum(
                        Resource.empty(),
                        instrumentationScopeInfo,
                        "name",
                        "description",
                        "1",
                        ImmutableSumData.create(
                            /* isMonotonic= */ true,
                            AggregationTemporality.CUMULATIVE,
                            Collections.singletonList(
                                ImmutableDoublePointData.create(123, 456, KV_ATTR, 5.0)))),
                    ImmutableMetricData.createDoubleSum(
                        Resource.empty(),
                        InstrumentationScopeInfo.empty(),
                        "name",
                        "description",
                        "1",
                        ImmutableSumData.create(
                            /* isMonotonic= */ true,
                            AggregationTemporality.CUMULATIVE,
                            Collections.singletonList(
                                ImmutableDoublePointData.create(123, 456, KV_ATTR, 5.0)))))))
        .satisfiesExactlyInAnyOrder(
            resourceMetrics -> {
              assertThat(resourceMetrics.getResource()).isEqualTo(resourceProto);
              assertThat(resourceMetrics.getSchemaUrl()).isEqualTo("http://resource.url");
              assertThat(resourceMetrics.getScopeMetricsList())
                  .containsExactlyInAnyOrder(
                      ScopeMetrics.newBuilder()
                          .setScope(scopeProto)
                          .addAllMetrics(ImmutableList.of(metricDoubleSum, metricDoubleSum))
                          .setSchemaUrl("http://url")
                          .build());
            },
            resourceMetrics -> {
              assertThat(resourceMetrics.getResource()).isEqualTo(emptyResourceProto);
              assertThat(resourceMetrics.getScopeMetricsList())
                  .containsExactlyInAnyOrder(
                      ScopeMetrics.newBuilder()
                          .setScope(emptyScopeProto)
                          .addAllMetrics(singletonList(metricDoubleSum))
                          .build(),
                      ScopeMetrics.newBuilder()
                          .setScope(scopeProto)
                          .addAllMetrics(singletonList(metricDoubleSum))
                          .setSchemaUrl("http://url")
                          .build());
            });
  }

  private static List<NumberDataPoint> toNumberDataPoints(
      MarshalerSource marshalerSource, Collection<? extends PointData> points) {
    return points.stream()
        .map(point -> parse(NumberDataPoint.getDefaultInstance(), marshalerSource.create(point)))
        .collect(Collectors.toList());
  }

  private static List<SummaryDataPoint> toSummaryDataPoints(
      MarshalerSource marshalerSource, Collection<SummaryPointData> points) {
    return points.stream()
        .map(point -> parse(SummaryDataPoint.getDefaultInstance(), marshalerSource.create(point)))
        .collect(Collectors.toList());
  }

  private static List<HistogramDataPoint> toHistogramDataPoints(
      MarshalerSource marshalerSource, Collection<HistogramPointData> points) {
    return points.stream()
        .map(point -> parse(HistogramDataPoint.getDefaultInstance(), marshalerSource.create(point)))
        .collect(Collectors.toList());
  }

  private static List<ExponentialHistogramDataPoint> toExponentialHistogramDataPoints(
      MarshalerSource marshalerSource, Collection<ExponentialHistogramPointData> points) {
    return points.stream()
        .map(
            point ->
                parse(
                    ExponentialHistogramDataPoint.getDefaultInstance(),
                    marshalerSource.create(point)))
        .collect(Collectors.toList());
  }

  private static Metric toProtoMetric(MarshalerSource marshalerSource, MetricData metricData) {
    return parse(Metric.getDefaultInstance(), marshalerSource.create(metricData));
  }

  private static List<ResourceMetrics> toProtoResourceMetrics(
      MarshalerSource marshalerSource, Collection<MetricData> metricDataList) {
    ExportMetricsServiceRequest exportRequest =
        parse(
            ExportMetricsServiceRequest.getDefaultInstance(),
            marshalerSource.create(metricDataList));
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

  private static <T> Marshaler createMarshaler(StatelessMarshaler<T> marshaler, T data) {
    return new Marshaler() {
      private final MarshalerContext context = new MarshalerContext();
      private final int size = marshaler.getBinarySerializedSize(data, context);

      @Override
      public int getBinarySerializedSize() {
        return size;
      }

      @Override
      protected void writeTo(Serializer output) throws IOException {
        context.resetReadIndex();
        marshaler.writeTo(output, data, context);
      }
    };
  }

  private enum MarshalerSource {
    STATEFUL_MARSHALER {
      @Override
      Marshaler create(PointData point) {
        return NumberDataPointMarshaler.create(point);
      }

      @Override
      Marshaler create(SummaryPointData point) {
        return SummaryDataPointMarshaler.create(point);
      }

      @Override
      Marshaler create(HistogramPointData point) {
        return HistogramDataPointMarshaler.create(point);
      }

      @Override
      Marshaler create(ExponentialHistogramPointData point) {
        return ExponentialHistogramDataPointMarshaler.create(point);
      }

      @Override
      Marshaler create(MetricData metric) {
        return MetricMarshaler.create(metric);
      }

      @Override
      Marshaler create(Collection<MetricData> metricDataList) {
        return MetricsRequestMarshaler.create(metricDataList);
      }
    },
    STATELESS_MARSHALER {
      @Override
      Marshaler create(PointData point) {
        return createMarshaler(NumberDataPointStatelessMarshaler.INSTANCE, point);
      }

      @Override
      Marshaler create(SummaryPointData point) {
        return createMarshaler(SummaryDataPointStatelessMarshaler.INSTANCE, point);
      }

      @Override
      Marshaler create(HistogramPointData point) {
        return createMarshaler(HistogramDataPointStatelessMarshaler.INSTANCE, point);
      }

      @Override
      Marshaler create(ExponentialHistogramPointData point) {
        return createMarshaler(ExponentialHistogramDataPointStatelessMarshaler.INSTANCE, point);
      }

      @Override
      Marshaler create(MetricData metric) {
        return createMarshaler(MetricStatelessMarshaler.INSTANCE, metric);
      }

      @Override
      Marshaler create(Collection<MetricData> metricDataList) {
        LowAllocationMetricsRequestMarshaler marshaler = new LowAllocationMetricsRequestMarshaler();
        marshaler.initialize(metricDataList);
        return marshaler;
      }
    };

    abstract Marshaler create(PointData point);

    abstract Marshaler create(SummaryPointData point);

    abstract Marshaler create(HistogramPointData point);

    abstract Marshaler create(ExponentialHistogramPointData point);

    abstract Marshaler create(MetricData metric);

    abstract Marshaler create(Collection<MetricData> metricDataList);
  }
}
